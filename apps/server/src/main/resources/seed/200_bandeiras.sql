-- Bandeiras de cartão de crédito (RN-CLI-005, DEC-16). A tabela pertence ao módulo clientes e o seed é do CLI-01
-- (DEC-14: faixa 2NN). IDs explícitos de 1 a 9999.
INSERT INTO bandeira (id, nome) VALUES
    (1, 'Visa'),
    (2, 'Mastercard'),
    (3, 'Elo'),
    (4, 'American Express'),
    (5, 'Hipercard');
