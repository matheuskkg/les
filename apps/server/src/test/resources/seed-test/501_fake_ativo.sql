-- Nos testes, o fake atende sem rede e sem chave (RNF-REC-001, DEC-03).
UPDATE provedor_ia SET ativo = true WHERE nome = 'fake';
