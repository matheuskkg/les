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

- **Nunca copie os exemplos do template** (ImoX, biblioteca, SISCAD, Houter/Premium, Struts/Hibernate/Oracle etc.).
  Todo texto, diagrama e decisão deve refletir o projeto real; reescrever frases do exemplo trocando nomes conta
  como cópia.
- Visão de alto nível: o DVP resume e referencia o DRS e os DECs, sem transcrevê-los. Cite IDs (`RF-VEN-001`,
  `DEC-12`) quando útil.
- Seja fiel ao sistema implementado e ao escopo do MVP (o que está fora do MVP aparece como fora do escopo).
- Português, sem placeholders ("a definir", "pendente"); se faltar informação, pergunte ao usuário.
