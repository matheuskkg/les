-- Entradas de estoque com custo, fornecedor e data fixa (RN-EST-001, DEC-15.3).
-- O preço de venda não é gravado: é calculado depois como maior custo × (1 + margem) (RN-EST-002).
-- Cada livro novo (21 a 26) tem duas entradas, com custos diferentes, para exercitar RN-EST-002: vale o maior.
-- O livro 20 fica sem entrada, com saldo zero, para os testes E2E.

INSERT INTO entrada_estoque (id, livro_id, fornecedor_id, quantidade, custo, data_entrada) VALUES
    (1, 1, 1, 12, 34.90, '2026-01-10'),
    (2, 1, 2, 8, 39.90, '2026-04-15'),
    (3, 2, 1, 10, 42.00, '2026-01-12'),
    (4, 2, 3, 6, 47.50, '2026-03-20'),
    (5, 2, 2, 5, 45.00, '2026-06-05'),
    (6, 3, 2, 14, 29.90, '2026-02-03'),
    (7, 4, 1, 7, 36.50, '2026-02-12'),
    (8, 5, 3, 15, 32.00, '2026-02-01'),
    (9, 5, 1, 10, 35.75, '2026-05-10'),
    (10, 6, 3, 9, 49.90, '2026-03-08'),
    (11, 7, 1, 6, 54.00, '2026-03-18'),
    (12, 8, 2, 11, 38.50, '2026-01-25'),
    (13, 8, 3, 7, 41.20, '2026-04-02'),
    (14, 9, 2, 13, 33.30, '2026-02-20'),
    (15, 10, 3, 8, 37.40, '2026-03-28'),
    (16, 11, 1, 20, 79.90, '2026-02-10'),
    (17, 11, 2, 12, 84.50, '2026-05-18'),
    (18, 11, 3, 9, 82.00, '2026-07-01'),
    (19, 12, 2, 10, 89.90, '2026-06-12'),
    (20, 13, 1, 6, 68.00, '2026-03-01'),
    (21, 13, 3, 5, 71.50, '2026-06-15'),
    (22, 14, 2, 18, 46.90, '2026-01-30'),
    (23, 15, 2, 12, 49.50, '2026-04-22'),
    (24, 16, 3, 9, 44.40, '2026-05-05'),
    (25, 17, 1, 8, 39.90, '2026-02-25'),
    (26, 18, 2, 7, 42.20, '2026-03-30'),
    (27, 19, 3, 5, 58.00, '2026-04-08'),
    (28, 21, 4, 8, 38.00, '2026-02-14'),
    (29, 21, 1, 10, 41.00, '2026-05-22'),
    (30, 22, 5, 9, 34.00, '2026-01-18'),
    (31, 22, 2, 7, 38.00, '2026-04-30'),
    (32, 23, 4, 6, 48.00, '2026-03-12'),
    (33, 23, 5, 8, 52.00, '2026-06-08'),
    (34, 24, 2, 10, 32.00, '2026-02-06'),
    (35, 24, 4, 8, 36.00, '2026-05-16'),
    (36, 25, 1, 5, 54.00, '2026-03-25'),
    (37, 25, 5, 7, 58.00, '2026-07-07'),
    (38, 26, 3, 9, 40.00, '2026-01-29'),
    (39, 26, 4, 6, 46.00, '2026-06-19');

-- Saldo por livro, derivado das entradas (DEC-15.3). Todo livro tem exatamente um saldo, inclusive o livro 20,
-- sem entradas, com quantidade zero. O id do saldo é o do livro, na faixa do seed.
INSERT INTO estoque (id, livro_id, quantidade)
SELECT l.id, l.id, COALESCE(SUM(e.quantidade), 0)
FROM livro l
LEFT JOIN entrada_estoque e ON e.livro_id = l.id
GROUP BY l.id;
