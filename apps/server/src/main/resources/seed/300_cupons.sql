-- Cupons do VEN-06 (RF-VEN-007, DEC-11, DEC-14, DEC-15.2). Datas relativas ao relógio dos testes e do E2E
-- (2026-10-24).
--
-- Promocionais, um de cada tipo, mais um vencido e um com valor mínimo de compra (subtotal dos livros, sem frete).
INSERT INTO cupom_promocional (id, codigo, tipo, valor, validade, valor_minimo) VALUES
    (1, 'BEMVINDO10', 'VALOR_FIXO', 10.00, DATE '2027-12-31', NULL),
    (2, 'LIVROS15', 'PERCENTUAL', 15.00, DATE '2027-12-31', 100.00),
    (3, 'FRETEGRATIS', 'FRETE_GRATIS', NULL, DATE '2027-12-31', NULL),
    (4, 'FRETE5', 'FRETE_VALOR_FIXO', 5.00, DATE '2027-12-31', NULL),
    (5, 'NATAL2025', 'VALOR_FIXO', 20.00, DATE '2025-12-31', NULL);

-- Cupons de troca. A Ana (cliente 1) tem os valores do exemplo do RN-VEN-006 (R$ 20, R$ 40 e R$ 35), um já usado e
-- um vencido; o Bruno (cliente 2) tem um.
INSERT INTO cupom (id, cliente_id, codigo, valor, validade, situacao, gerado_em, version) VALUES
    (1, 1, 'TROCA-ANA-20', 20.00, NULL, 'DISPONIVEL', TIMESTAMP '2026-03-10 14:00:00', 0),
    (2, 1, 'TROCA-ANA-40', 40.00, NULL, 'DISPONIVEL', TIMESTAMP '2026-05-02 09:30:00', 0),
    (3, 1, 'TROCA-ANA-35', 35.00, NULL, 'DISPONIVEL', TIMESTAMP '2026-07-21 18:15:00', 0),
    (4, 1, 'TROCA-ANA-USADO', 15.00, NULL, 'USADO', TIMESTAMP '2026-01-15 11:00:00', 1),
    (5, 1, 'TROCA-ANA-VENCIDO', 25.00, DATE '2026-06-30', 'DISPONIVEL', TIMESTAMP '2025-12-30 16:45:00', 0),
    (6, 2, 'TROCA-BRUNO-15', 15.00, NULL, 'DISPONIVEL', TIMESTAMP '2026-08-05 10:00:00', 0);
