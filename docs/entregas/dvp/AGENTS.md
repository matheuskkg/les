# AGENTS.md

Documento de Visão de Projeto (DVP) do e-commerce de livros, entregável da disciplina LES (task `[DOC-03]`, issue #3).

## Fontes

- Estrutura: template [`materiais-fatec/dvp/dvp.md`](../../materiais-fatec/dvp/dvp.md).
- Como preencher cada seção: [`materiais-fatec/dvp/guia-elaboracao-dvp.md`](../../materiais-fatec/dvp/guia-elaboracao-dvp.md).
  Leia a seção do guia antes de escrever a seção correspondente.
- Conteúdo: requisitos em [`docs/requisitos/`](../../requisitos/README.md), decisões em
  [`docs/decisoes/`](../../decisoes/README.md), arquitetura em [`docs/arquitetura.md`](../../arquitetura.md),
  modelo de dados em [`docs/modelo/`](../../modelo/) e o escopo do MVP no `AGENTS.md` da raiz.

## Organização

- Um arquivo Markdown por tópico de primeiro nível do template, numerado na ordem do documento
  (`00-capa.md`, `01-objetivo.md`, `02-necessidades-de-negocio.md`...). Subtópicos (ex.: 1.1 Escopo, 1.2 Referências)
  ficam dentro do arquivo do tópico pai.
- `00-capa.md` contém nome do projeto, histórico de versões (Data, Versão, Descrição, Autor, Revisor), a tabela
  Cliente/Documento/Data/Autor e a página de assinaturas. Atualize o histórico a cada revisão relevante.
- Imagens e diagramas em `imagens/` nesta pasta, referenciados por caminho relativo.
- Mantenha a numeração e os títulos das seções do template.

## Regras de conteúdo

- **Autocontido:** quem lê só o DVP, sem acesso ao código e sem contexto prévio do projeto, precisa entender o
  sistema. Explique cada decisão (o que foi decidido e por quê) e cada limitação no próprio texto; o ID do DEC ou do
  requisito é só referência complementar, nunca substitui a explicação. Nomes de classes, métodos, rotas e códigos
  HTTP só quando ajudam a ler um diagrama, e sempre com o papel explicado em português.
- **Nunca copie os exemplos do template** (ImoX, biblioteca, SISCAD, Houter/Premium, Struts/Hibernate/Oracle etc.).
  Todo texto, diagrama e decisão deve refletir o projeto real; reescrever frases do exemplo trocando nomes conta
  como cópia.
- Visão de alto nível: o DVP resume e referencia o DRS e os DECs, sem transcrevê-los. Cite IDs (`RF-VEN-001`,
  `DEC-12`) quando útil.
- O documento final é entregue em **.docx**: não há links nem caminhos para arquivos do repositório (`docs/...`,
  `apps/...`, `AGENTS.md`, nomes de arquivo `.java`/`.tsx`/`.md`). Cite os documentos pelo nome e ID (ex.: "DRS v0.6,
  grupo Vendas", "DEC-12 – Pedido, checkout e trocas"); a lista deles fica em 1.2 Referências. Nomes de classes,
  pacotes e módulos podem aparecer como conceitos do sistema, sem caminho. Imagens só de `imagens/`.
- Seja fiel ao sistema implementado e ao escopo do MVP (o que está fora do MVP aparece como fora do escopo).
- Português, sem placeholders ("a definir", "pendente"); se faltar informação, pergunte ao usuário.

## Diagramas

- UML 2.5, desenhado à mão em XML no draw.io: um único `imagens/dvp.drawio`, uma página por diagrama, com o PNG
  exportado ao lado (`imagens/<pagina>.png`). Rótulos em português; classes, pacotes e módulos com os nomes do código.
- Por seção: componentes e organização genérica de um módulo hexagonal (7), casos de uso agrupados em casos-mãe, com os atores Visitante, Cliente, Administrador,
  Tempo (agendador) e os sistemas externos simulados/IA (8), pacotes de um módulo exemplar (`vendas`), classes
  principais, estados de `Pedido`/`Troca` e sequência de "Finalizar compra" (9), implantação (10).
- Visão de Dados (12): reaproveita os PNGs gerados em `docs/modelo/` (`gerar_mer.py`), sem redesenhar o MER.
- As camadas Apresentação/Negócio/Persistência do template são mapeadas para a arquitetura hexagonal, e o texto
  diz isso explicitamente: `infraestrutura/web` + `apps/web` (apresentação), `dominio` + `aplicacao` (negócio),
  `infraestrutura/persistencia` (persistência).
