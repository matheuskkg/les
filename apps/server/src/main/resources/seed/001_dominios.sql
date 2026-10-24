-- Domínios do catálogo e do estoque (RNF-LIV-002, DEC-14). IDs explícitos de 1 a 9999.

INSERT INTO autor (id, nome) VALUES
    (1, 'Machado de Assis'),
    (2, 'Clarice Lispector'),
    (3, 'J. R. R. Tolkien'),
    (4, 'Isaac Asimov'),
    (5, 'Robert C. Martin'),
    (6, 'Yuval Noah Harari'),
    (7, 'Ursula K. Le Guin'),
    (8, 'Jane Austen');

INSERT INTO editora (id, nome) VALUES
    (1, 'Companhia das Letras'),
    (2, 'Rocco'),
    (3, 'HarperCollins Brasil'),
    (4, 'Editora Sextante'),
    (5, 'Novatec');

INSERT INTO categoria (id, nome) VALUES
    (1, 'Romance'),
    (2, 'Ficção Científica'),
    (3, 'Fantasia'),
    (4, 'Tecnologia'),
    (5, 'História');

INSERT INTO grupo_precificacao (id, nome, margem) VALUES
    (1, 'Padrão', 0.2000),
    (2, 'Popular', 0.3500),
    (3, 'Premium', 0.5000);

INSERT INTO fornecedor (id, nome) VALUES
    (1, 'Distribuidora Livro Bom'),
    (2, 'Atacado das Letras'),
    (3, 'Editoras Reunidas Distribuição');
