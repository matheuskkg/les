# 5. Premissas

As premissas são as informações consideradas verdadeiras para planejar e executar o projeto; elas orientam as
decisões e serão usadas como entrada nos processos seguintes (especificação, implementação e testes). Se alguma se
mostrar falsa no decorrer da disciplina, o plano e o escopo devem ser reavaliados.

- O projeto é orientado pelo professor Rodrigo Rocha Silva (LES, FATEC, 2026), responsável pela leitura e revisão
  deste DVP e pela gestão dos marcos do semestre (seção 4.3.3).
- O projeto tem um único integrante, Matheus Kazuo Koike Graciliano, que acumula os papéis de analista e
  desenvolvedor (seção 6); os prazos da disciplina consideram essa capacidade, o que reforça o corte do escopo
  feito no MVP.
- A operadora de cartão (DEC-05.2) e o cálculo de frete (DEC-05.3) são simulados por serviços *fake* com regras
  determinísticas, sem integração real com transportadoras ou gateways de pagamento.
- O assistente de recomendação usa provedores de IA generativa com cota gratuita
  (Gemini, Groq, OpenRouter e NVIDIA NIM), configurados por variáveis de ambiente; as cotas são limitadas, por isso
  o sistema mantém cadeia de provedores e gravação das métricas de chamada. O adapter determinístico *fake* fica
  disponível no desenvolvimento e nos testes, mas não é usado na apresentação (DEC-03, Guia de Configuração da IA).
  Os testes automatizados não dependem de rede nem de chave.
- O sistema não entra em produção na disciplina; o banco de dados pode ser resetado
  livremente, o schema e o seed podem mudar sem compatibilidade com dados anteriores, e os dados de teste,
  inclusive cartões fictícios, ficam no seed SQL executado na inicialização (DEC-14).
- Os dados do catálogo existem desde o seed: livros, estoque e tabelas de domínio (autor, editora, categoria, grupo
  de precificação, fornecedor, bandeiras); por isso o CRUD de livros e a entrada manual de estoque
  estão fora do MVP (seção 4.3.3).
- O desenvolvimento é feito em uma máquina Linux com Java 21, Maven, Node (Vite) e PostgreSQL via Docker Compose; os testes E2E usam o Chrome instalado localmente (DEC-02).
- Os marcos de 2026 listados na seção 4.3.3 valem como compromissos do cronograma; a
  entrega final do MVP cobre vendas completas (carrinho, checkout, pagamento e trocas), estoque derivado,
  auditoria, análise e recomendação, conforme o escopo da seção 4.
