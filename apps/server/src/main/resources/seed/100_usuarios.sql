-- Usuários do sistema (DEC-14: faixa 1NN). O administrador é criado só pelo seed (DEC-08, DEC-10).
-- Senha de teste do administrador: Admin@123 (hash BCrypt gerado com custo 12, RNF-CLI-003).
-- O administrador não tem telefone (DEC-10).
-- Os demais IDs da faixa do seed (2 a 9999, DEC-14) ficam livres para os clientes de teste do CLI-01.
-- Dois administradores de apoio recebem IDs 100 e 101, separados da faixa dos clientes, e a mesma senha Admin@123
-- (hash BCrypt de custo 12, RNF-CLI-003). Servem para a apresentação e para testes de mais de uma conta ADMIN.
insert into usuario (id, email, senha_hash, papel, inativado_em)
values (1, 'admin@livraria.test', '$2a$12$tBL/lwtgoCE9aT4jnhIMt.708q6gcto91xPB4CKYHcrN5zYPrys.e',
        'ADMIN', null),
       (100, 'admin2@livraria.test', '$2a$12$tBL/lwtgoCE9aT4jnhIMt.708q6gcto91xPB4CKYHcrN5zYPrys.e',
        'ADMIN', null),
       (101, 'admin3@livraria.test', '$2a$12$tBL/lwtgoCE9aT4jnhIMt.708q6gcto91xPB4CKYHcrN5zYPrys.e',
        'ADMIN', null);
