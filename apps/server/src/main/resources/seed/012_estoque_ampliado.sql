-- Entradas de estoque dos livros adicionais (RN-EST-001, DEC-15.3, DEC-21).
-- O preço de venda não é gravado: é calculado como maior custo × (1 + margem) (RF-EST-002, RN-EST-002).
-- Os livros 37, 38, 47 e 50 têm duas entradas com custos diferentes, para exercitar o maior custo da RN-EST-002.
-- Os livros 1 a 26 e as entradas 1 a 39 de 003_estoque.sql ficam intactos.

INSERT INTO entrada_estoque (id, livro_id, fornecedor_id, quantidade, custo, data_entrada) VALUES
    (40, 27, 6, 8, 44.00, '2026-02-05'),
    (41, 28, 6, 10, 32.50, '2026-02-08'),
    (42, 29, 7, 7, 38.00, '2026-02-11'),
    (43, 30, 7, 12, 35.90, '2026-02-14'),
    (44, 31, 8, 15, 28.00, '2026-02-17'),
    (45, 32, 8, 9, 41.00, '2026-02-20'),
    (46, 33, 9, 20, 22.00, '2026-02-23'),
    (47, 34, 9, 8, 47.00, '2026-02-26'),
    (48, 35, 10, 6, 55.00, '2026-03-01'),
    (49, 36, 10, 5, 89.00, '2026-03-04'),
    (50, 37, 6, 6, 34.00, '2026-03-07'),
    (51, 37, 7, 5, 37.50, '2026-03-10'),
    (52, 38, 8, 7, 43.00, '2026-03-13'),
    (53, 38, 9, 6, 46.00, '2026-03-16'),
    (54, 39, 10, 8, 36.00, '2026-03-19'),
    (55, 40, 6, 9, 39.00, '2026-03-22'),
    (56, 41, 7, 18, 24.00, '2026-03-25'),
    (57, 42, 8, 11, 33.50, '2026-03-28'),
    (58, 43, 9, 16, 26.00, '2026-03-31'),
    (59, 44, 9, 7, 49.00, '2026-04-03'),
    (60, 45, 10, 6, 58.00, '2026-04-06'),
    (61, 46, 10, 4, 92.00, '2026-04-09'),
    (62, 47, 6, 6, 35.00, '2026-04-12'),
    (63, 47, 7, 5, 38.00, '2026-04-15'),
    (64, 48, 7, 9, 40.00, '2026-04-18'),
    (65, 49, 8, 8, 42.00, '2026-04-21'),
    (66, 50, 8, 8, 45.00, '2026-04-24'),
    (67, 50, 9, 6, 48.50, '2026-04-27'),
    (68, 51, 7, 14, 29.00, '2026-04-30'),
    (69, 52, 8, 10, 34.50, '2026-05-03'),
    (70, 53, 10, 7, 37.00, '2026-05-06'),
    (71, 54, 6, 8, 41.50, '2026-05-09'),
    (72, 55, 9, 9, 46.00, '2026-05-12'),
    (73, 56, 10, 8, 39.50, '2026-05-15');

-- Saldo dos livros adicionais, derivado das entradas (DEC-15.3). O id do saldo é o do livro, na faixa do seed.
-- 003_estoque.sql já criou as linhas dos livros 1 a 26; aqui só entram as linhas 27 a 56.
INSERT INTO estoque (id, livro_id, quantidade)
SELECT l.id, l.id, COALESCE(SUM(e.quantidade), 0)
FROM livro l
LEFT JOIN entrada_estoque e ON e.livro_id = l.id
WHERE l.id BETWEEN 27 AND 56
GROUP BY l.id;
