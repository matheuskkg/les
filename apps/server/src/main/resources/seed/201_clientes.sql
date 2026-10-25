-- Clientes de teste do CLI-01 (RF-CLI-001, DEC-10, DEC-14). Os usuários de login dos clientes também entram aqui,
-- depois do seed 100_usuarios.sql: o administrador é o usuário 1 e os clientes usam os ids 2 a 6.
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

INSERT INTO usuario (id, email, senha_hash, papel, inativado_em, telefone_tipo, telefone_ddd, telefone_numero) VALUES
    (2, 'ana.souza@livraria.test', '$2a$12$NxCJW3SRKvtvtlmaQeG3Xu89XL0hoXVzbuEQnLzsnD2F.G7mMKlcG', 'CLIENTE', NULL, 'CELULAR', '11', '988887001'),
    (3, 'bruno.lima@livraria.test', '$2a$12$NxCJW3SRKvtvtlmaQeG3Xu89XL0hoXVzbuEQnLzsnD2F.G7mMKlcG', 'CLIENTE', NULL, 'CELULAR', '21', '988887002'),
    (4, 'carla.mendes@livraria.test', '$2a$12$NxCJW3SRKvtvtlmaQeG3Xu89XL0hoXVzbuEQnLzsnD2F.G7mMKlcG', 'CLIENTE', '2026-10-20 12:00:00', 'CELULAR', '31', '988887003'),
    (5, 'diego.rocha@livraria.test', '$2a$12$NxCJW3SRKvtvtlmaQeG3Xu89XL0hoXVzbuEQnLzsnD2F.G7mMKlcG', 'CLIENTE', '2026-09-01 12:00:00', 'CELULAR', '41', '988887004'),
    (6, 'elisa.prado@livraria.test', '$2a$12$NxCJW3SRKvtvtlmaQeG3Xu89XL0hoXVzbuEQnLzsnD2F.G7mMKlcG', 'CLIENTE', NULL, 'CELULAR', '51', '988887005');

INSERT INTO cliente (id, usuario_id, nome, genero, data_nascimento, cpf) VALUES
    (1, 2, 'Ana Souza', 'FEMININO', '1990-03-14', '52998224725'),
    (2, 3, 'Bruno Lima', 'MASCULINO', '1985-07-22', '11144477735'),
    (3, 4, 'Carla Mendes', 'FEMININO', '1992-11-02', '12345678909'),
    (4, 5, 'Diego Rocha', 'MASCULINO', '1978-01-30', '98765432100'),
    (5, 6, 'Elisa Prado', 'NAO_INFORMADO', '1995-05-09', '39053344705');

INSERT INTO endereco (id, cliente_id, nome_curto, tipo_endereco, tipo_logradouro, logradouro, numero, bairro,
                      cep, cidade, uf, pais, observacoes) VALUES
    (1, 1, 'Casa', 'CASA', 'RUA', 'Rua das Flores', '123', 'Jardim Paulista', '01415000', 'São Paulo', 'SP', 'Brasil', NULL),
    (2, 1, 'Trabalho', 'TRABALHO', 'AVENIDA', 'Avenida Paulista', '1000', 'Bela Vista', '01310100', 'São Paulo', 'SP', 'Brasil', '8º andar'),
    (3, 2, 'Casa', 'CASA', 'RUA', 'Rua das Laranjeiras', '45', 'Laranjeiras', '22240000', 'Rio de Janeiro', 'RJ', 'Brasil', NULL),
    (4, 3, 'Casa', 'CASA', 'RUA', 'Rua da Bahia', '900', 'Centro', '30160010', 'Belo Horizonte', 'MG', 'Brasil', NULL),
    (5, 4, 'Casa', 'CASA', 'RUA', 'Rua XV de Novembro', '250', 'Centro', '80020310', 'Curitiba', 'PR', 'Brasil', NULL),
    (6, 5, 'Casa', 'CASA', 'RUA', 'Rua dos Andradas', '700', 'Centro Histórico', '90020120', 'Porto Alegre', 'RS', 'Brasil', NULL),
    (7, 5, 'Trabalho', 'TRABALHO', 'ALAMEDA', 'Alameda Santos', '500', 'Cerqueira César', '01418100', 'São Paulo', 'SP', 'Brasil', 'Recepção');

INSERT INTO cartao (id, cliente_id, bandeira_id, numero, nome_impresso, validade, preferencial) VALUES
    (1, 1, 1, '4111111111111111', 'ANA SOUZA', '2030-12', TRUE),
    (2, 2, 2, '5555555555554444', 'BRUNO LIMA', '2029-08', TRUE),
    (3, 3, 3, '6362970000457013', 'CARLA MENDES', '2028-05', TRUE),
    (4, 5, 4, '378282246310005', 'ELISA PRADO', '2020-01', TRUE),
    (5, 5, 5, '6062825624080000', 'ELISA PRADO', '2032-12', FALSE);
