-- Clientes de teste do CLI-01 (RF-CLI-001, DEC-10, DEC-14). Os usuários de login dos clientes também entram aqui,
-- depois do seed 100_usuarios.sql: o administrador é o usuário 1, os administradores de apoio são 100 e 101 e os
-- clientes usam os ids 2 a 11 (clientes 1 a 10). Os cinco clientes novos (6 a 10) dão volume ao histórico de vendas
-- gerado por scripts/gerar_seed_historico.py; Ana e Bruno continuam sem pedidos para os testes que partem deles.
--
-- Senha de todos os clientes de teste: Senha@123
-- O hash abaixo é BCrypt com custo 12 (RNF-CLI-003) e foi conferido com o CodificadorDeSenhaBCrypt do projeto.
--
-- Situação: os prazos são calculados na consulta (DEC-10), então as datas são fixas:
--   - Carla (usuário 4) foi inativada em 2026-10-20T12:00 (America/Sao_Paulo). Com o relógio fixado em
--     2026-10-24T15:00:00Z (12:00 em America/Sao_Paulo), ela está INATIVA, com 4 dias (menos de 7). Os E2E e os
--     testes de integração do cliente usam esse mesmo instante;
--   - Diego (usuário 5) foi inativado em 2026-09-01T12:00 (America/Sao_Paulo): bem antes do prazo de 7 dias, então
--     a conta está ENCERRADA e o CPF, o e-mail e o telefone dele estão livres para um novo cadastro.
--
-- Cartões: sem código de segurança (DEC-10). Elisa tem um cartão vencido em 2020-01 (DEC-06/DEC-05.2) e outro com
-- número terminado em 0000 (DEC-05.2). Todos os clientes têm ao menos um endereço, de qualquer tipo (DEC-10).
-- Os cinco clientes novos ficam em UFs ainda não usadas no seed (SC, BA, PE, PA e GO), com gênero NAO_INFORMADO e
-- datas de nascimento fora da faixa 1990-1995, para não alterar os filtros conferidos nos testes de consulta.

INSERT INTO usuario (id, email, senha_hash, papel, inativado_em, telefone_tipo, telefone_ddd, telefone_numero) VALUES
    (2, 'ana.souza@livraria.test', '$2a$12$NxCJW3SRKvtvtlmaQeG3Xu89XL0hoXVzbuEQnLzsnD2F.G7mMKlcG', 'CLIENTE', NULL, 'CELULAR', '11', '988887001'),
    (3, 'bruno.lima@livraria.test', '$2a$12$NxCJW3SRKvtvtlmaQeG3Xu89XL0hoXVzbuEQnLzsnD2F.G7mMKlcG', 'CLIENTE', NULL, 'CELULAR', '21', '988887002'),
    (4, 'carla.mendes@livraria.test', '$2a$12$NxCJW3SRKvtvtlmaQeG3Xu89XL0hoXVzbuEQnLzsnD2F.G7mMKlcG', 'CLIENTE', '2026-10-20 12:00:00', 'CELULAR', '31', '988887003'),
    (5, 'diego.rocha@livraria.test', '$2a$12$NxCJW3SRKvtvtlmaQeG3Xu89XL0hoXVzbuEQnLzsnD2F.G7mMKlcG', 'CLIENTE', '2026-09-01 12:00:00', 'CELULAR', '41', '988887004'),
    (6, 'elisa.prado@livraria.test', '$2a$12$NxCJW3SRKvtvtlmaQeG3Xu89XL0hoXVzbuEQnLzsnD2F.G7mMKlcG', 'CLIENTE', NULL, 'CELULAR', '51', '988887005'),
    (7, 'fernanda.alves@livraria.test', '$2a$12$NxCJW3SRKvtvtlmaQeG3Xu89XL0hoXVzbuEQnLzsnD2F.G7mMKlcG', 'CLIENTE', NULL, 'CELULAR', '47', '988887006'),
    (8, 'gustavo.nunes@livraria.test', '$2a$12$NxCJW3SRKvtvtlmaQeG3Xu89XL0hoXVzbuEQnLzsnD2F.G7mMKlcG', 'CLIENTE', NULL, 'CELULAR', '71', '988887007'),
    (9, 'helena.castro@livraria.test', '$2a$12$NxCJW3SRKvtvtlmaQeG3Xu89XL0hoXVzbuEQnLzsnD2F.G7mMKlcG', 'CLIENTE', NULL, 'CELULAR', '81', '988887008'),
    (10, 'ivan.barbosa@livraria.test', '$2a$12$NxCJW3SRKvtvtlmaQeG3Xu89XL0hoXVzbuEQnLzsnD2F.G7mMKlcG', 'CLIENTE', NULL, 'CELULAR', '91', '988887009'),
    (11, 'juliana.dias@livraria.test', '$2a$12$NxCJW3SRKvtvtlmaQeG3Xu89XL0hoXVzbuEQnLzsnD2F.G7mMKlcG', 'CLIENTE', NULL, 'CELULAR', '62', '988887010');

INSERT INTO cliente (id, usuario_id, nome, genero, data_nascimento, cpf) VALUES
    (1, 2, 'Ana Souza', 'FEMININO', '1990-03-14', '52998224725'),
    (2, 3, 'Bruno Lima', 'MASCULINO', '1985-07-22', '11144477735'),
    (3, 4, 'Carla Mendes', 'FEMININO', '1992-11-02', '12345678909'),
    (4, 5, 'Diego Rocha', 'MASCULINO', '1978-01-30', '98765432100'),
    (5, 6, 'Elisa Prado', 'NAO_INFORMADO', '1995-05-09', '39053344705'),
    (6, 7, 'Fernanda Alves', 'NAO_INFORMADO', '1968-04-12', '12345678062'),
    (7, 8, 'Gustavo Nunes', 'NAO_INFORMADO', '1972-09-23', '23456789092'),
    (8, 9, 'Helena Castro', 'NAO_INFORMADO', '1982-12-05', '34567890175'),
    (9, 10, 'Ivan Barbosa', 'NAO_INFORMADO', '1986-02-18', '45678901249'),
    (10, 11, 'Juliana Dias', 'NAO_INFORMADO', '1988-07-30', '56789012303');

INSERT INTO endereco (id, cliente_id, nome_curto, tipo_endereco, tipo_logradouro, logradouro, numero, bairro,
                      cep, cidade, uf, pais, observacoes) VALUES
    (1, 1, 'Casa', 'CASA', 'RUA', 'Rua das Flores', '123', 'Jardim Paulista', '01415000', 'São Paulo', 'SP', 'Brasil', NULL),
    (2, 1, 'Trabalho', 'TRABALHO', 'AVENIDA', 'Avenida Paulista', '1000', 'Bela Vista', '01310100', 'São Paulo', 'SP', 'Brasil', '8º andar'),
    (3, 2, 'Casa', 'CASA', 'RUA', 'Rua das Laranjeiras', '45', 'Laranjeiras', '22240000', 'Rio de Janeiro', 'RJ', 'Brasil', NULL),
    (4, 3, 'Casa', 'CASA', 'RUA', 'Rua da Bahia', '900', 'Centro', '30160010', 'Belo Horizonte', 'MG', 'Brasil', NULL),
    (5, 4, 'Casa', 'CASA', 'RUA', 'Rua XV de Novembro', '250', 'Centro', '80020310', 'Curitiba', 'PR', 'Brasil', NULL),
    (6, 5, 'Casa', 'CASA', 'RUA', 'Rua dos Andradas', '700', 'Centro Histórico', '90020120', 'Porto Alegre', 'RS', 'Brasil', NULL),
    (7, 5, 'Trabalho', 'TRABALHO', 'ALAMEDA', 'Alameda Santos', '500', 'Cerqueira César', '01418100', 'São Paulo', 'SP', 'Brasil', 'Recepção'),
    (8, 6, 'Casa', 'CASA', 'RUA', 'Rua do Lago', '120', 'Centro', '88010000', 'Florianópolis', 'SC', 'Brasil', NULL),
    (9, 7, 'Casa', 'CASA', 'AVENIDA', 'Avenida Sete de Setembro', '2340', 'Vitória', '40060000', 'Salvador', 'BA', 'Brasil', NULL),
    (10, 8, 'Casa', 'CASA', 'RUA', 'Rua da Aurora', '765', 'Boa Vista', '50050040', 'Recife', 'PE', 'Brasil', NULL),
    (11, 9, 'Casa', 'CASA', 'AVENIDA', 'Avenida Presidente Vargas', '890', 'Centro', '66010000', 'Belém', 'PA', 'Brasil', NULL),
    (12, 10, 'Casa', 'CASA', 'RUA', 'Rua 24 de Outubro', '315', 'Setor Marista', '74175020', 'Goiânia', 'GO', 'Brasil', NULL);

INSERT INTO cartao (id, cliente_id, bandeira_id, numero, nome_impresso, validade, preferencial) VALUES
    (1, 1, 1, '4111111111111111', 'ANA SOUZA', '2030-12', TRUE),
    (2, 2, 2, '5555555555554444', 'BRUNO LIMA', '2029-08', TRUE),
    (3, 3, 3, '6362970000457013', 'CARLA MENDES', '2028-05', TRUE),
    (4, 5, 4, '378282246310005', 'ELISA PRADO', '2020-01', TRUE),
    (5, 5, 5, '6062825624080000', 'ELISA PRADO', '2032-12', FALSE),
    (6, 6, 1, '4555555555555557', 'FERNANDA ALVES', '2031-05', TRUE),
    (7, 7, 2, '5555444444444442', 'GUSTAVO NUNES', '2030-09', TRUE),
    (8, 8, 3, '6362970000457017', 'HELENA CASTRO', '2029-11', TRUE),
    (9, 9, 4, '378282246310006', 'IVAN BARBOSA', '2032-03', TRUE),
    (10, 10, 5, '6062825624080004', 'JULIANA DIAS', '2031-07', FALSE);
