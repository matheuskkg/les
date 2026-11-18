# Roteiro de revisão: `docs/entregas/`

Abra esta página quando o diff toca documentos formais da disciplina. Aqui a fonte da regra é o `AGENTS.md` da própria
pasta, não o `AGENTS.md` da raiz sozinho.

## Leitura obrigatória

1. `docs/entregas/dvp/AGENTS.md` (o `CLAUDE.md` da pasta só o inclui) — fontes, organização, conteúdo e diagramas do
   DVP. Se o diff tocar outro entregável, procure o `AGENTS.md` da pasta correspondente.
2. O template e o guia apontados nesse `AGENTS.md`, em `docs/materiais-fatec/`, para a seção alterada.
3. A skill `humanizar-texto` para toda prosa: é ela que define o que soa escrito por IA e como revisar. Carregue-a e
   aplique-a ao trecho alterado; dizer que ela "deve ser aplicada depois" não conclui a revisão.
4. `AGENTS.md` da raiz para o escopo do MVP e as convenções de domínio.
5. Requisitos, decisões e arquitetura quando o texto descreve o sistema: `docs/requisitos/README.md`,
   `docs/decisoes/README.md`, `docs/arquitetura.md`.

## O que conferir

### Conteúdo

- Autocontido: quem lê só o documento entende o sistema. ID de DEC ou requisito é referência complementar, nunca
  substitui a explicação do que foi decidido e por quê.
- Fiel ao sistema implementado e ao MVP: o que está fora aparece como fora do escopo.
- Sem copiar exemplos do template (ImoX, biblioteca, SISCAD e congêneres) nem trocar só os nomes.
- Sem placeholders ("a definir", "pendente"). Se faltar informação, a revisão pergunta.
- Visão de alto nível: o DVP resume e referencia DRS e DECs, sem transcrevê-los.

### Forma

- Um arquivo Markdown por tópico de primeiro nível do template, numerado; subtópicos ficam no arquivo pai. Numeração
  e títulos do template preservados.
- `00-capa.md` com histórico de versões atualizado quando a revisão é relevante, tabela Cliente/Documento/Data/Autor
  e página de assinaturas.
- Texto revisado com `humanizar-texto`: sem travessão como pontuação geral, sem listas onde cabe parágrafo, sem
  adjetivos vazios nem frases-resumo.
- Versão final em `.docx`: sem caminhos do repositório (`docs/...`, `apps/...`, nomes de `.java`/`.tsx`/`.md`, links).
  Documentos são citados pelo nome e ID, e a lista fica em Referências. Nomes de classes e módulos podem aparecer
  como conceito, sem caminho.

### Diagramas e modelos

- UML 2.5 em draw.io: um `imagens/dvp.drawio` com uma página por diagrama e o PNG exportado ao lado, rótulos em
  português.
- Camadas do template mapeadas para a hexagonal, com o texto dizendo isso explicitamente.
- MER reaproveitado de `docs/modelo/` (gerado por `gerar_mer.py`), sem redesenhar.
- Se o diff tocar `.pptx`, use a skill `pptx` para abrir e conferir o conteúdo; não edite o binário no texto.

### Rastreabilidade

- Mudança de requisito implementado atualiza a matriz em `docs/requisitos/<grupo>.md`: status, onde no código e
  observação. O texto do requisito não muda; ambiguidade vira nota na coluna Observação.

## Anti-padrões específicos de entregas

- Revisar DVP sem abrir o `AGENTS.md` da pasta e o guia da seção alterada.
- Deixar passar "a definir", caminho de repositório no texto que vira .docx ou exemplo do template.
- Corrigir prosa na mão sem aplicar `humanizar-texto`.
- Pedir para transcrever DRS ou DEC no documento em vez de resumir e citar.
- Tratar diagrama como imagem qualquer: o fonte é o `.drawio`, com o PNG ao lado.
