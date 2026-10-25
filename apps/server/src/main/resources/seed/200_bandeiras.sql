-- Bandeiras de cartão de crédito (RN-CLI-005, DEC-16). A tabela pertence ao módulo clientes e o seed é do CLI-01
-- (DEC-14: faixa 2NN). IDs explícitos de 1 a 9999.
INSERT INTO bandeira (id, nome, prefixos, tamanhos) VALUES
    (1, 'Visa', '4', '13,16,19'),
    (2, 'Mastercard', '51-55,2221-2720', '16'),
    (3, 'Elo', '401178,401179,431274,438935,451416,457393,457631,457632,504175,506699-506778,509000-509999,627780,636297,636368,650031-650033,650035-650051,650405-650439,650485-650538,650541-650598,650700-650718,650720-650727,650901-650978,651652-651679,655000-655019,655021-655058', '16'),
    (4, 'American Express', '34,37', '15'),
    (5, 'Hipercard', '606282,3841', '16,19');
