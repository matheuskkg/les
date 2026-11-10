-- Cadeia padrão de provedores de IA (RNF-REC-001, DEC-03). A tabela pertence ao módulo recomendacao e o seed é do
-- REC-01 (DEC-14: faixa 5NN). IDs explícitos de 1 a 9999; os provedores reais ficam ativos, na ordem de preferência
-- da cadeia. O fake fica inativo por padrão: só o profile dev (ou os testes) o ativa, para a apresentação usar IA real.
INSERT INTO provedor_ia (id, nome, ordem, ativo) VALUES
    (1, 'gemini', 1, true),
    (2, 'groq', 2, true),
    (3, 'openrouter', 3, true),
    (5, 'nvidia', 4, true),
    (4, 'fake', 5, false);
