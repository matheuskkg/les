-- Usuários do sistema (DEC-14: faixa 1NN). O administrador é criado só pelo seed (DEC-08, DEC-10).
-- Senha de teste do administrador: Admin@123 (hash BCrypt gerado com custo 12, RNF-CLI-003).
-- Os demais IDs da faixa do seed (2 a 9999, DEC-14) ficam livres para os clientes de teste do CLI-01.
insert into usuario (id, email, senha_hash, papel, inativado_em, telefone_tipo, telefone_ddd, telefone_numero)
values (1, 'admin@livraria.test', '$2a$12$tBL/lwtgoCE9aT4jnhIMt.708q6gcto91xPB4CKYHcrN5zYPrys.e',
        'ADMIN', null, 'CELULAR', '11', '900000000');
