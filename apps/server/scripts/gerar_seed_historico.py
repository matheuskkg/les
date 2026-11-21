#!/usr/bin/env python3
"""Gera src/main/resources/seed/400_pedidos_historicos.sql (DEC-14, faixa 4NN, card ANA-01).

Pedidos de novembro/2023 a outubro/2026 (36 meses no banco) para a análise de vendas (RF-ANA-001) e o ranking. A
janela de 24 meses da RN-ANA-002 limita o RECORTE consultado no gráfico, não o histórico armazenado (DEC-20): por isso
o seed pode ir além dela. Os clientes do histórico são a Carla (3), a Elisa (5) e os cinco clientes novos do seed
201_clientes.sql (6 a 10). A Ana e o Bruno ficam sem histórico, porque os testes partem deles sem pedidos.

O sorteio usa sementes fixas, então o arquivo gerado é sempre o mesmo. O laço original (nov/2024 a out/2026), a Carla
e a Elisa foram mantidos sem alteração: os testes de controller e os roteiros E2E conferem valores exatos deles. A
massa adicional entra em três laços posteriores, cada um com a própria semente, só para os clientes novos: o primeiro
densifica os 17 meses livres dos 24 finais; o segundo cobre os 12 meses novos (nov/2023 a out/2024); o terceiro
(ALT-02) dá mais volume aos meses de nov/2024 a set/2026, exceto mar/2025, usando os livros novos do catálogo.
Nenhum valor ou ID já conferido muda.

O laço da ALT-02 sortea só os livros cujas categorias são todas novas (6 a 17). Assim os totais das cinco categorias
originais, conferidos valor a valor nos testes da análise e no E2E de apresentação, permanecem exatamente os mesmos.

Março/2025 não é sorteado: os pedidos dele são escritos à mão para que os testes da análise tenham valores esperados
independentes do código:

- Romance 95,76 e História 95,76: 2 × Dom Casmurro (duas categorias, valor cheio em cada), com frete e promocional
  BEMVINDO10, que não entram;
- Ficção Científica 96,00: 3 × Fundação a 48,00, com troca concluída (TROCADO) de 1 unidade, descontada;
- Fantasia 80,00: 2 × O Hobbit a 40,00, com troca TROCA AUTORIZADA de 1 unidade, ainda não descontada;
- Tecnologia 0,00: o único pedido (Código Limpo) foi REPROVADA.

Uso: python3 scripts/gerar_seed_historico.py (a partir de apps/server).
"""
import json
import random
from datetime import date, datetime, timedelta
from decimal import Decimal
from pathlib import Path

SAIDA = Path(__file__).resolve().parent.parent / 'src/main/resources/seed/400_pedidos_historicos.sql'
ULTIMO_DIA = date(2026, 10, 20)
EDITORAS = {1: 'Companhia das Letras', 2: 'Rocco', 3: 'HarperCollins Brasil', 4: 'Editora Sextante', 5: 'Novatec',
            6: 'Editora Record', 7: 'Leya', 8: 'Editora Claridade', 9: 'Editora Estrela', 10: 'Editora Horizonte',
            11: 'Editora Mosaico', 12: 'Editora Nova Aurora', 13: 'Editora Pórtico', 14: 'Editora Tinta Viva',
            15: 'Editora Veredas'}
CATEGORIAS = {1: 'Romance', 2: 'Ficção Científica', 3: 'Fantasia', 4: 'Tecnologia', 5: 'História',
              6: 'Biografia', 7: 'Mistério', 8: 'Suspense', 9: 'Poesia', 10: 'Literatura Infantil',
              11: 'Literatura Juvenil', 12: 'Autoajuda', 13: 'Economia', 14: 'Filosofia', 15: 'Artes',
              16: 'Ciência', 17: 'Aventura'}

# id: (título, autores, editora, edição, ISBN, categorias, preço de venda histórico)
LIVROS = {
    1: ('Dom Casmurro', ['Machado de Assis'], 1, '1ª edição', '9788500010019', [1, 5], '47.88'),
    2: ('Contos Brasileiros', ['Machado de Assis', 'Clarice Lispector'], 1, '2ª edição', '9788500010026', [1, 5], '64.13'),
    3: ('A Hora da Estrela', ['Clarice Lispector'], 1, '1ª edição', '9788500010033', [1], '40.37'),
    4: ('A Paixão Segundo G.H.', ['Clarice Lispector'], 2, '3ª edição', '9788500010040', [1], '43.80'),
    5: ('O Hobbit', ['J. R. R. Tolkien'], 3, '1ª edição', '9788500010057', [3], '42.90'),
    6: ('O Senhor dos Anéis: A Sociedade do Anel', ['J. R. R. Tolkien'], 3, '1ª edição', '9788500010064', [3], '59.88'),
    7: ('O Silmarillion', ['J. R. R. Tolkien'], 2, '2ª edição', '9788500010071', [3, 5], '72.90'),
    8: ('Fundação', ['Isaac Asimov'], 2, '1ª edição', '9788500010088', [2], '49.44'),
    9: ('Eu, Robô', ['Isaac Asimov'], 2, '1ª edição', '9788500010095', [2], '44.96'),
    10: ('O Fim da Eternidade', ['Isaac Asimov'], 2, '1ª edição', '9788500010101', [2], '44.88'),
    11: ('Código Limpo', ['Robert C. Martin'], 5, '1ª edição', '9788500010118', [4], '126.75'),
    12: ('Arquitetura Limpa', ['Robert C. Martin'], 5, '1ª edição', '9788500010125', [4], '134.85'),
    13: ('A História da Tecnologia', ['Robert C. Martin', 'Yuval Noah Harari'], 4, '1ª edição', '9788500010132', [4, 5], '107.25'),
    14: ('Sapiens: Uma Breve História da Humanidade', ['Yuval Noah Harari'], 1, '1ª edição', '9788500010149', [5], '56.28'),
    15: ('Homo Deus: Uma Breve História do Amanhã', ['Yuval Noah Harari'], 1, '1ª edição', '9788500010156', [5, 4], '59.40'),
    16: ('21 Lições para o Século 21', ['Yuval Noah Harari'], 1, '1ª edição', '9788500010163', [5, 4], '59.94'),
    17: ('A Mão Esquerda da Escuridão', ['Ursula K. Le Guin'], 2, '2ª edição', '9788500010170', [2, 3], '47.88'),
    18: ('Os Despossuídos', ['Ursula K. Le Guin'], 2, '1ª edição', '9788500010187', [2], '56.97'),
    # Livros acrescentados pela ALT-02. O preço histórico sai de maior custo × (1 + margem) (RF-EST-002, RN-EST-002).
    27: ('O Jardim das Horas', ['Ana Beatriz Ribeiro'], 8, '1ª edição', '9788500010279', [6, 14], '66.00'),
    28: ('O Último Trem para Bruges', ['Caio Monteiro'], 9, '1ª edição', '9788500010286', [7, 1], '43.88'),
    29: ('Cartas do Silêncio', ['Diana Prado'], 10, '2ª edição', '9788500010293', [9], '57.00'),
    30: ('A Casa das Marés', ['Eduardo Lins'], 11, '1ª edição', '9788500010309', [8, 1], '48.47'),
    31: ('Pequenos Astronautas', ['Fábio Vilhena'], 12, '1ª edição', '9788500010316', [10, 2], '33.60'),
    32: ('O Mapa das Estrelas', ['Helena Marques'], 13, '2ª edição', '9788500010323', [17, 11], '55.35'),
    33: ('A Arte de Recomeçar', ['Ivo Sampaio'], 14, '1ª edição', '9788500010330', [12], '25.30'),
    34: ('Dinheiro e Liberdade', ['Juliana Freitas'], 14, '1ª edição', '9788500010347', [13, 12], '63.45'),
    35: ('O Caminho das Ideias', ['Laura Mendes', 'Diana Prado'], 15, '1ª edição', '9788500010354', [14, 5], '82.50'),
    36: ('Cores do Brasil', ['Marcelo Tavares'], 15, '1ª edição', '9788500010361', [15, 5], '151.30'),
    37: ('O Enigma da Biblioteca', ['Nívia Andrade'], 9, '1ª edição', '9788500010378', [7, 11], '50.63'),
    38: ('Sob o Céu de Marte', ['Otávio Bandeira'], 13, '1ª edição', '9788500010385', [2, 17], '62.10'),
    39: ('Versos para a Chuva', ['Paula Renó'], 10, '2ª edição', '9788500010392', [9], '54.00'),
    40: ('O Guardião do Farol', ['Rafael Coutinho'], 11, '1ª edição', '9788500010408', [8, 1], '52.65'),
    41: ('Histórias da Vovó', ['Sérgio Arantes'], 12, '1ª edição', '9788500010415', [10], '27.60'),
    42: ('A Fábrica de Sonhos', ['Ana Beatriz Ribeiro'], 13, '1ª edição', '9788500010422', [11, 17], '45.23'),
    43: ('Viver com Propósito', ['Ivo Sampaio'], 14, '1ª edição', '9788500010439', [12], '29.90'),
    44: ('Mercado em Movimento', ['Juliana Freitas'], 14, '2ª edição', '9788500010446', [13], '66.15'),
    45: ('Pensar o Mundo', ['Laura Mendes', 'Sérgio Arantes'], 15, '1ª edição', '9788500010453', [14, 5], '87.00'),
    46: ('Mestres da Pintura', ['Marcelo Tavares'], 15, '1ª edição', '9788500010460', [15], '156.40'),
    47: ('O Caso da Rua Silenciosa', ['Caio Monteiro'], 9, '1ª edição', '9788500010477', [7, 8], '51.30'),
    48: ('Diário de um Viajante', ['Diana Prado'], 8, '1ª edição', '9788500010484', [17, 6], '54.00'),
    49: ('A Luz das Montanhas', ['Eduardo Lins'], 10, '1ª edição', '9788500010491', [1, 9], '63.00'),
    50: ('Robôs e Humanos', ['Fábio Vilhena', 'Otávio Bandeira'], 13, '1ª edição', '9788500010507', [2, 14], '65.48'),
    51: ('Primeiros Passos na Ciência', ['Helena Marques'], 12, '1ª edição', '9788500010514', [16, 10], '33.35'),
    52: ('O Segredo da Flor Azul', ['Nívia Andrade'], 13, '2ª edição', '9788500010521', [11, 17], '46.58'),
    53: ('Poemas do Amanhã', ['Paula Renó'], 10, '1ª edição', '9788500010538', [9], '55.50'),
    54: ('A Cidade Invisível', ['Rafael Coutinho'], 11, '1ª edição', '9788500010545', [8, 7], '56.03'),
    55: ('Tecnologia do Cotidiano', ['Otávio Bandeira', 'Fábio Vilhena'], 14, '1ª edição', '9788500010552',
         [16, 13], '62.10'),
    56: ('A Última Página', ['Sérgio Arantes'], 11, '1ª edição', '9788500010569', [1, 7], '59.25'),
}

# Os laços anteriores à ALT-02 devem sortear exatamente os mesmos livros de antes: usar LIVROS aqui deslocaria o
# random.sample e mudaria pedidos, valores e datas que testes e E2E conferem.
LIVROS_ORIGINAIS = {livro_id: livro for livro_id, livro in LIVROS.items() if livro_id <= 18}

CLIENTES = {
    3: {
        'json': {'id': 3, 'nome': 'Carla Mendes', 'cpf': '12345678909', 'email': 'carla.mendes@livraria.test',
                 'telefone': '(31) 98888-7003'},
        'endereco': {'id': 4, 'nomeCurto': 'Casa', 'tipoEndereco': 'CASA', 'tipoLogradouro': 'RUA',
                     'logradouro': 'Rua da Bahia', 'numero': '900', 'bairro': 'Centro', 'cep': '30160010',
                     'cidade': 'Belo Horizonte', 'uf': 'MG', 'pais': 'Brasil'},
        'cartao': {'id': 3, 'bandeira': 'Elo', 'ultimosDigitos': '7013'},
        'frete': '18.00',
    },
    5: {
        'json': {'id': 5, 'nome': 'Elisa Prado', 'cpf': '39053344705', 'email': 'elisa.prado@livraria.test',
                 'telefone': '(51) 98888-7005'},
        'endereco': {'id': 6, 'nomeCurto': 'Casa', 'tipoEndereco': 'CASA', 'tipoLogradouro': 'RUA',
                     'logradouro': 'Rua dos Andradas', 'numero': '700', 'bairro': 'Centro Histórico',
                     'cep': '90020120', 'cidade': 'Porto Alegre', 'uf': 'RS', 'pais': 'Brasil'},
        'cartao': {'id': 4, 'bandeira': 'American Express', 'ultimosDigitos': '0005'},
        # O cartão final 0000 é o que a operadora simulada reprova (DEC-05.2).
        'cartao_reprovado': {'id': 5, 'bandeira': 'Hipercard', 'ultimosDigitos': '0000'},
        'frete': '22.00',
    },
    # Clientes novos do seed 201_clientes.sql. Só eles recebem os pedidos adicionais, para que o histórico da Carla
    # e da Elisa — conferido valor a valor pelos testes e pelo E2E — continue exatamente o mesmo.
    6: {
        'json': {'id': 6, 'nome': 'Fernanda Alves', 'cpf': '12345678062',
                 'email': 'fernanda.alves@livraria.test', 'telefone': '(47) 98888-7006'},
        'endereco': {'id': 8, 'nomeCurto': 'Casa', 'tipoEndereco': 'CASA', 'tipoLogradouro': 'RUA',
                     'logradouro': 'Rua do Lago', 'numero': '120', 'bairro': 'Centro', 'cep': '88010000',
                     'cidade': 'Florianópolis', 'uf': 'SC', 'pais': 'Brasil'},
        'cartao': {'id': 6, 'bandeira': 'Visa', 'ultimosDigitos': '5557'},
        'frete': '16.00',
    },
    7: {
        'json': {'id': 7, 'nome': 'Gustavo Nunes', 'cpf': '23456789092',
                 'email': 'gustavo.nunes@livraria.test', 'telefone': '(71) 98888-7007'},
        'endereco': {'id': 9, 'nomeCurto': 'Casa', 'tipoEndereco': 'CASA', 'tipoLogradouro': 'AVENIDA',
                     'logradouro': 'Avenida Sete de Setembro', 'numero': '2340', 'bairro': 'Vitória',
                     'cep': '40060000', 'cidade': 'Salvador', 'uf': 'BA', 'pais': 'Brasil'},
        'cartao': {'id': 7, 'bandeira': 'Mastercard', 'ultimosDigitos': '4442'},
        'frete': '20.00',
    },
    8: {
        'json': {'id': 8, 'nome': 'Helena Castro', 'cpf': '34567890175',
                 'email': 'helena.castro@livraria.test', 'telefone': '(81) 98888-7008'},
        'endereco': {'id': 10, 'nomeCurto': 'Casa', 'tipoEndereco': 'CASA', 'tipoLogradouro': 'RUA',
                     'logradouro': 'Rua da Aurora', 'numero': '765', 'bairro': 'Boa Vista', 'cep': '50050040',
                     'cidade': 'Recife', 'uf': 'PE', 'pais': 'Brasil'},
        'cartao': {'id': 8, 'bandeira': 'Elo', 'ultimosDigitos': '7017'},
        'frete': '18.00',
    },
    9: {
        'json': {'id': 9, 'nome': 'Ivan Barbosa', 'cpf': '45678901249',
                 'email': 'ivan.barbosa@livraria.test', 'telefone': '(91) 98888-7009'},
        'endereco': {'id': 11, 'nomeCurto': 'Casa', 'tipoEndereco': 'CASA', 'tipoLogradouro': 'AVENIDA',
                     'logradouro': 'Avenida Presidente Vargas', 'numero': '890', 'bairro': 'Centro',
                     'cep': '66010000', 'cidade': 'Belém', 'uf': 'PA', 'pais': 'Brasil'},
        'cartao': {'id': 9, 'bandeira': 'American Express', 'ultimosDigitos': '0006'},
        'frete': '24.00',
    },
    10: {
        'json': {'id': 10, 'nome': 'Juliana Dias', 'cpf': '56789012303',
                 'email': 'juliana.dias@livraria.test', 'telefone': '(62) 98888-7010'},
        'endereco': {'id': 12, 'nomeCurto': 'Casa', 'tipoEndereco': 'CASA', 'tipoLogradouro': 'RUA',
                     'logradouro': 'Rua 24 de Outubro', 'numero': '315', 'bairro': 'Setor Marista',
                     'cep': '74175020', 'cidade': 'Goiânia', 'uf': 'GO', 'pais': 'Brasil'},
        'cartao': {'id': 10, 'bandeira': 'Hipercard', 'ultimosDigitos': '0004'},
        'frete': '19.00',
    },
}

PROMOCIONAIS = {1: ('BEMVINDO10', 'VALOR_FIXO'), 4: ('FRETE5', 'FRETE_VALOR_FIXO'),
                7: ('FRETE10', 'FRETE_VALOR_FIXO')}

# Meses sem valor fixado por teste de controller nem por roteiro E2E. O histórico continua cobrindo os 24 meses; estes
# são os meses em que a massa adicional pode entrar sem alterar os valores que a apresentação mostra.
MESES_EXTRA = [(2024, 12), (2025, 2), (2025, 5), (2025, 7), (2025, 8), (2025, 9), (2025, 10), (2025, 11),
               (2026, 1), (2026, 2), (2026, 3), (2026, 4), (2026, 5), (2026, 6), (2026, 7), (2026, 8), (2026, 9)]
# Os 12 meses que antecedem o histórico original. A consulta da análise continua limitada a 24 meses (RN-ANA-002);
# estes meses ampliam a janela do banco, não a da tela (DEC-20).
MESES_ANTIGOS = [(2023, 11), (2023, 12)] + [(2024, mes) for mes in range(1, 11)]
NOVOS_CLIENTES = [6, 7, 8, 9, 10]
# Livros da ALT-02 cujas categorias são todas novas (6 a 17). Só eles entram no laço novo: testes da análise e o E2E
# de apresentação conferem, valor a valor, os totais das categorias 1 a 5 em vários meses.
LIVROS_NOVOS = [livro_id for livro_id, livro in LIVROS.items()
                if livro_id >= 27 and all(categoria >= 6 for categoria in livro[5])]
# nov/2024 a set/2026, sem mar/2025 (mês de referência da análise) e sem out/2026 (cuja data máxima e status são
# conferidos). Mantém a data mínima, a máxima e a janela estendida antes de nov/2024 intactas.
MESES_RECENTES = [(ano, mes) for ano in (2024, 2025, 2026) for mes in range(1, 13)
                  if (2024, 11) <= (ano, mes) <= (2026, 9) and (ano, mes) != (2025, 3)]


def js(valor):
    texto = json.dumps(valor, ensure_ascii=False, separators=(',', ':'))
    return "JSON '" + texto.replace("'", "''") + "'"


def ts(momento):
    return "TIMESTAMP '" + momento.strftime('%Y-%m-%d %H:%M:%S') + "'"


def dinheiro(valor):
    return str(Decimal(valor).quantize(Decimal('0.01')))


def livro_json(livro_id):
    titulo, autores, editora, edicao, isbn, categorias, _ = LIVROS[livro_id]
    return {'id': livro_id, 'titulo': titulo, 'autores': autores, 'editora': EDITORAS[editora], 'edicao': edicao,
            'isbn': isbn, 'categorias': [{'id': c, 'nome': CATEGORIAS[c]} for c in categorias]}


class Seed:
    def __init__(self):
        self.pedidos, self.itens, self.pagamentos = [], [], []
        self.cupons_pedido, self.usos, self.trocas, self.itens_troca, self.cupons = [], [], [], [], []

    def pedido(self, cliente_id, realizado_em, status, itens, promocional=None, troca=None):
        """itens: [(livro_id, quantidade, preço)]; troca: (status, [(índice do item, quantidade)])."""
        cliente = CLIENTES[cliente_id]
        pedido_id = len(self.pedidos) + 1
        subtotal = sum(Decimal(preco) * quantidade for _, quantidade, preco in itens)
        frete = Decimal(cliente['frete'])
        desconto = Decimal('0')
        if promocional == 1:
            desconto = Decimal('10.00')
        elif promocional == 4:
            desconto = Decimal('5.00')
        elif promocional == 7:
            desconto = Decimal('10.00')
        total = subtotal + frete - desconto
        entregue_em = None
        if status == 'ENTREGUE':
            entregue_em = (realizado_em + timedelta(days=4)).replace(hour=15, minute=0)
        self.pedidos.append(
            f"({pedido_id}, NULL, {cliente_id}, '{status}', {ts(realizado_em)}, "
            f"{ts(entregue_em) if entregue_em else 'NULL'}, {js(cliente['json'])}, {js(cliente['endereco'])}, "
            f"{js(cliente['endereco'])}, {dinheiro(subtotal)}, {dinheiro(frete)}, {dinheiro(desconto)}, "
            f"{dinheiro(total)})")
        ids_dos_itens = []
        for livro_id, quantidade, preco in itens:
            item_id = len(self.itens) + 1
            ids_dos_itens.append((item_id, livro_id, quantidade, preco))
            self.itens.append(f"({item_id}, {pedido_id}, {js(livro_json(livro_id))}, {quantidade}, {dinheiro(preco)})")
        cartao = cliente['cartao_reprovado'] if status == 'REPROVADA' and 'cartao_reprovado' in cliente \
            else cliente['cartao']
        self.pagamentos.append(f"({len(self.pagamentos) + 1}, {pedido_id}, {js(cartao)}, {dinheiro(total)})")
        if promocional:
            codigo, tipo = PROMOCIONAIS[promocional]
            self.cupons_pedido.append(
                f"({len(self.cupons_pedido) + 1}, {pedido_id}, "
                f"{js({'id': promocional, 'codigo': codigo, 'categoria': 'PROMOCIONAL', 'tipo': tipo})}, "
                f"{dinheiro(desconto)})")
            if status != 'REPROVADA':
                self.usos.append(f"({len(self.usos) + 1}, {promocional}, {cliente_id}, {pedido_id})")
        if troca:
            self.troca(pedido_id, cliente_id, entregue_em, ids_dos_itens, *troca)
        return pedido_id

    def troca(self, pedido_id, cliente_id, entregue_em, itens, status, escolhidos):
        troca_id = len(self.trocas) + 1
        solicitada_em = entregue_em + timedelta(days=2)
        valor = sum(Decimal(itens[indice][3]) * quantidade for indice, quantidade in escolhidos)
        cupom_id, valor_cupom, motivo = 'NULL', 'NULL', 'NULL'
        if status == 'TROCADO':
            cupom_id = 100 + len(self.cupons)
            valor_cupom = dinheiro(valor)
            self.cupons.append(
                f"({cupom_id}, {cliente_id}, 'TROCA-H{troca_id}', {dinheiro(valor)}, NULL, 'DISPONIVEL', "
                f"{ts(solicitada_em + timedelta(days=5))}, 0)")
        if status == 'TROCA_RECUSADA':
            motivo = "'Livro com marcas de uso.'"
        self.trocas.append(f"({troca_id}, {pedido_id}, {cupom_id}, '{status}', {ts(solicitada_em)}, {motivo}, "
                           f"{valor_cupom})")
        for posicao, (indice, quantidade) in enumerate(escolhidos):
            volta = 'NULL'
            if status == 'TROCADO':
                volta = 'TRUE' if posicao % 2 == 0 else 'FALSE'
            self.itens_troca.append(
                f"({len(self.itens_troca) + 1}, {troca_id}, {itens[indice][0]}, {quantidade}, {volta})")

    def sql(self):
        blocos = [
            ('pedido', 'id, checkout_id, cliente_id, status, realizado_em, entregue_em, cliente, endereco_entrega, '
                       'endereco_cobranca, subtotal_itens, frete, desconto_promocional, total', self.pedidos),
            ('item_pedido', 'id, pedido_id, livro, quantidade, preco_unitario', self.itens),
            ('pagamento_cartao', 'id, pedido_id, cartao, valor', self.pagamentos),
            ('cupom_pedido', 'id, pedido_id, cupom, valor_abatido', self.cupons_pedido),
            ('uso_cupom_promocional', 'id, cupom_promocional_id, cliente_id, pedido_id', self.usos),
            ('cupom', 'id, cliente_id, codigo, valor, validade, situacao, gerado_em, version', self.cupons),
            ('troca', 'id, pedido_id, cupom_id, status, solicitada_em, motivo_recusa, valor_cupom', self.trocas),
            ('item_troca', 'id, troca_id, item_pedido_id, quantidade, volta_estoque', self.itens_troca),
        ]
        partes = [
            '-- Pedidos históricos de nov/2023 a out/2026 para a análise de vendas (RF-ANA-001, DEC-12, DEC-14, DEC-16).',
            '-- Gerado por scripts/gerar_seed_historico.py, que explica o conteúdo; não edite à mão.',
            '',
        ]
        for tabela, colunas, linhas in blocos:
            for inicio in range(0, len(linhas), 200):
                partes.append(f'INSERT INTO {tabela} ({colunas}) VALUES')
                partes.append(',\n'.join('    ' + linha for linha in linhas[inicio:inicio + 200]) + ';')
                partes.append('')
        return '\n'.join(partes)


def meses():
    ano, mes = 2024, 11
    while (ano, mes) <= (2026, 10):
        yield ano, mes
        ano, mes = (ano + 1, 1) if mes == 12 else (ano, mes + 1)


def main():
    sorteio = random.Random(2026)
    seed = Seed()
    promocional_da_elisa = False
    for ano, mes in meses():
        if (ano, mes) == (2025, 3):
            # Mês de referência dos testes: ver o docstring.
            seed.pedido(3, datetime(2025, 3, 5, 10, 0), 'ENTREGUE', [(1, 2, '47.88')], promocional=1)
            seed.pedido(5, datetime(2025, 3, 12, 14, 30), 'ENTREGUE', [(8, 3, '48.00')],
                        troca=('TROCADO', [(0, 1)]))
            seed.pedido(5, datetime(2025, 3, 18, 9, 15), 'ENTREGUE', [(5, 2, '40.00')],
                        troca=('TROCA_AUTORIZADA', [(0, 1)]))
            seed.pedido(3, datetime(2025, 3, 25, 16, 45), 'REPROVADA', [(11, 1, '120.00')])
            continue
        for _ in range(sorteio.randint(8, 13)):
            dia = sorteio.randint(1, 28)
            realizado = datetime(ano, mes, dia, sorteio.randint(8, 21), sorteio.choice([0, 15, 30, 45]))
            if realizado.date() > ULTIMO_DIA:
                continue
            cliente = sorteio.choice([3, 5])
            livros = sorteio.sample(sorted(LIVROS_ORIGINAIS), sorteio.choice([1, 1, 2, 2, 3]))
            itens = [(livro, sorteio.choice([1, 1, 1, 2, 3]), LIVROS[livro][6]) for livro in livros]
            if sorteio.random() < 0.07:
                status = 'REPROVADA'
            elif realizado.date() >= ULTIMO_DIA - timedelta(days=4):
                status = 'APROVADA'
            elif realizado.date() >= ULTIMO_DIA - timedelta(days=8):
                status = 'EM_TRANSPORTE'
            else:
                status = 'ENTREGUE'
            promocional = None
            if cliente == 5 and not promocional_da_elisa and status == 'ENTREGUE' and (ano, mes) >= (2025, 6):
                promocional, promocional_da_elisa = 4, True
            troca = None
            if status == 'ENTREGUE' and sorteio.random() < 0.08:
                escolhidos = [(i, sorteio.randint(1, itens[i][1])) for i in range(len(itens)) if sorteio.random() < 0.7]
                if escolhidos:
                    troca = (sorteio.choice(['TROCADO', 'TROCADO', 'TROCA_RECUSADA']), escolhidos)
            seed.pedido(cliente, realizado, status, itens, promocional=promocional, troca=troca)

    # Massa adicional: semente separada para não deslocar o sorteio original; só clientes novos e só meses sem valor
    # fixado por teste de controller ou roteiro E2E. O período continua sendo nov/2024 a out/2026 (24 meses,
    # RN-ANA-002), agora com bem mais pedidos e trocas distribuídos por ele.
    extra = random.Random(2027)
    # DEC-15.2: cada cliente usa cada promocional no máximo uma vez (há unique constraint no banco).
    usos_promocionais = set()
    for ano, mes in MESES_EXTRA:
        for _ in range(extra.randint(18, 26)):
            dia = extra.randint(1, 28)
            realizado = datetime(ano, mes, dia, extra.randint(8, 21), extra.choice([0, 15, 30, 45]))
            cliente = extra.choice(NOVOS_CLIENTES)
            livros = extra.sample(sorted(LIVROS_ORIGINAIS), extra.choice([1, 1, 2, 2, 3]))
            itens = [(livro, extra.choice([1, 1, 1, 2, 3]), LIVROS[livro][6]) for livro in livros]
            if extra.random() < 0.08:
                status = 'REPROVADA'
            elif (ano, mes) == (2026, 9) and dia >= 22:
                status = extra.choice(['APROVADA', 'EM_TRANSPORTE'])
            else:
                status = 'ENTREGUE'
            promocional = None
            if status != 'REPROVADA':
                disponiveis = [p for p in (4, 7) if (p, cliente) not in usos_promocionais]
                if disponiveis and extra.random() < 0.15:
                    promocional = extra.choice(disponiveis)
                    usos_promocionais.add((promocional, cliente))
            troca = None
            if status == 'ENTREGUE' and extra.random() < 0.20:
                escolhidos = [(i, extra.randint(1, itens[i][1])) for i in range(len(itens)) if extra.random() < 0.7]
                if escolhidos:
                    trocas = ['TROCADO', 'TROCADO', 'TROCADO', 'TROCA_RECUSADA', 'TROCA_AUTORIZADA']
                    troca = (extra.choice(trocas), escolhidos)
            seed.pedido(cliente, realizado, status, itens, promocional=promocional, troca=troca)

    # Janela estendida do banco: nov/2023 a out/2024, 12 meses novos com a mesma densidade dos meses densificados.
    # Semente própria para não deslocar os laços anteriores, e nenhum mês fica sem venda. A RN-ANA-002 continua
    # limitando o recorte da análise a 24 meses; aqui se amplia só o histórico disponível (DEC-20).
    antigos = random.Random(2030)
    for ano, mes in MESES_ANTIGOS:
        for _ in range(antigos.randint(16, 22)):
            dia = antigos.randint(1, 28)
            realizado = datetime(ano, mes, dia, antigos.randint(8, 21), antigos.choice([0, 15, 30, 45]))
            cliente = antigos.choice(NOVOS_CLIENTES)
            livros = antigos.sample(sorted(LIVROS_ORIGINAIS), antigos.choice([1, 1, 2, 2, 3]))
            itens = [(livro, antigos.choice([1, 1, 1, 2, 3]), LIVROS[livro][6]) for livro in livros]
            status = 'REPROVADA' if antigos.random() < 0.08 else 'ENTREGUE'
            promocional = None
            if status != 'REPROVADA':
                disponiveis = [p for p in (4, 7) if (p, cliente) not in usos_promocionais]
                if disponiveis and antigos.random() < 0.10:
                    promocional = antigos.choice(disponiveis)
                    usos_promocionais.add((promocional, cliente))
            troca = None
            if status == 'ENTREGUE' and antigos.random() < 0.18:
                escolhidos = [(i, antigos.randint(1, itens[i][1])) for i in range(len(itens)) if antigos.random() < 0.7]
                if escolhidos:
                    trocas = ['TROCADO', 'TROCADO', 'TROCA_RECUSADA', 'TROCA_AUTORIZADA']
                    troca = (antigos.choice(trocas), escolhidos)
            seed.pedido(cliente, realizado, status, itens, promocional=promocional, troca=troca)

    # ALT-02: mais volume de vendas sobre os livros novos, com APROVADA, EM_TRANSPORTE, REPROVADA e ENTREGUE,
    # trocas em três status e cupons promocionais. Semente própria para não deslocar os laços anteriores: todos os
    # IDs e valores já conferidos por testes e pelo E2E ficam exatamente iguais, e os novos pedidos começam em 848.
    recentes = random.Random(2031)
    for ano, mes in MESES_RECENTES:
        for _ in range(recentes.randint(12, 18)):
            dia = recentes.randint(1, 28)
            realizado = datetime(ano, mes, dia, recentes.randint(8, 21), recentes.choice([0, 15, 30, 45]))
            cliente = recentes.choice(NOVOS_CLIENTES)
            livros = recentes.sample(sorted(LIVROS_NOVOS), recentes.choice([1, 1, 2, 2, 3]))
            itens = [(livro, recentes.choice([1, 1, 1, 2, 3]), LIVROS[livro][6]) for livro in livros]
            if recentes.random() < 0.10:
                status = 'REPROVADA'
            elif (ano, mes) == (2026, 9) and dia >= 22:
                status = recentes.choice(['APROVADA', 'EM_TRANSPORTE'])
            else:
                status = 'ENTREGUE'
            promocional = None
            if status != 'REPROVADA':
                disponiveis = [p for p in (4, 7) if (p, cliente) not in usos_promocionais]
                if disponiveis and recentes.random() < 0.12:
                    promocional = recentes.choice(disponiveis)
                    usos_promocionais.add((promocional, cliente))
            troca = None
            if status == 'ENTREGUE' and recentes.random() < 0.22:
                escolhidos = [(i, recentes.randint(1, itens[i][1])) for i in range(len(itens))
                              if recentes.random() < 0.7]
                if escolhidos:
                    trocas = ['TROCADO', 'TROCADO', 'TROCA_RECUSADA', 'TROCA_AUTORIZADA']
                    troca = (recentes.choice(trocas), escolhidos)
            seed.pedido(cliente, realizado, status, itens, promocional=promocional, troca=troca)

    SAIDA.write_text(seed.sql() + '\n', encoding='utf-8')
    print(f'{SAIDA.name}: {len(seed.pedidos)} pedidos, {len(seed.itens)} itens, {len(seed.trocas)} trocas, '
          f'{len(seed.cupons)} cupons de troca')


if __name__ == '__main__':
    main()
