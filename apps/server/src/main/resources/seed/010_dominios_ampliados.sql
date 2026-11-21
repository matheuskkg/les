-- Ampliação dos domínios do catálogo (RNF-LIV-002, DEC-14, DEC-21). IDs explícitos de 1 a 9999.
-- Acrescenta dados novos sem alterar os já existentes de 001_dominios.sql.

INSERT INTO autor (id, nome) VALUES
    (15, 'Ana Beatriz Ribeiro'),
    (16, 'Caio Monteiro'),
    (17, 'Diana Prado'),
    (18, 'Eduardo Lins'),
    (19, 'Fábio Vilhena'),
    (20, 'Helena Marques'),
    (21, 'Ivo Sampaio'),
    (22, 'Juliana Freitas'),
    (23, 'Laura Mendes'),
    (24, 'Marcelo Tavares'),
    (25, 'Nívia Andrade'),
    (26, 'Otávio Bandeira'),
    (27, 'Paula Renó'),
    (28, 'Rafael Coutinho'),
    (29, 'Sérgio Arantes');

INSERT INTO editora (id, nome) VALUES
    (8, 'Editora Claridade'),
    (9, 'Editora Estrela'),
    (10, 'Editora Horizonte'),
    (11, 'Editora Mosaico'),
    (12, 'Editora Nova Aurora'),
    (13, 'Editora Pórtico'),
    (14, 'Editora Tinta Viva'),
    (15, 'Editora Veredas');

INSERT INTO categoria (id, nome) VALUES
    (6, 'Biografia'),
    (7, 'Mistério'),
    (8, 'Suspense'),
    (9, 'Poesia'),
    (10, 'Literatura Infantil'),
    (11, 'Literatura Juvenil'),
    (12, 'Autoajuda'),
    (13, 'Economia'),
    (14, 'Filosofia'),
    (15, 'Artes'),
    (16, 'Ciência'),
    (17, 'Aventura');

INSERT INTO grupo_precificacao (id, nome, margem) VALUES
    (4, 'Econômico', 0.1500),
    (5, 'Colecionador', 0.7000);

INSERT INTO fornecedor (id, nome) VALUES
    (6, 'Distribuidora Nova Era'),
    (7, 'Livraria Central Atacado'),
    (8, 'Grupo Editorial Sul'),
    (9, 'Distribuidora Horizonte'),
    (10, 'Casa do Livro Atacado');
