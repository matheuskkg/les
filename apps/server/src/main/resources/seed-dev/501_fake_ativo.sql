-- No profile dev, o fake atende sem rede e sem chave (RNF-REC-001, DEC-03). Na apresentação ele fica inativo, para
-- uma falha dos provedores reais aparecer como falha em vez de ser mascarada pelo fake.
UPDATE provedor_ia SET ativo = true WHERE nome = 'fake';
