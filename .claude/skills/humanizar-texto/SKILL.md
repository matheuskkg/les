---
name: humanizar-texto
description: Revisa e escreve texto de documentos formais (DVP, especificação de caso de uso, plano de testes, relatórios e demais entregas em docs/entregas/) para soar escrito por uma pessoa, sem marcas típicas de IA. Use sempre que redigir, revisar ou delegar a escrita de um documento formal do projeto, e antes de dar uma seção como pronta.
---

# Humanizar texto de documentos formais

Os documentos de entrega são lidos pelo professor como trabalho do aluno. Texto com cara de IA (travessões em toda
frase, listas para tudo, adjetivos vazios) passa a impressão de cópia e tira credibilidade, mesmo quando o conteúdo
está certo. O objetivo é um português acadêmico simples, direto e variado, como o de um estudante cuidadoso.

## Quando aplicar

- Ao escrever qualquer seção de documento formal (em `docs/entregas/` ou que vá virar .docx/PDF).
- Ao revisar texto produzido por subagente ou pelo opencode: aplique esta revisão antes de aceitar o resultado.
- Ao delegar a escrita, inclua no prompt as regras da seção "Marcas a eliminar" (o delegado não lê esta skill).

Não se aplica a código, DECs, `AGENTS.md`, matrizes de rastreabilidade nem mensagens de commit.

## Marcas a eliminar

1. **Travessão (—) como pontuação geral.** Troque por vírgula, dois-pontos, parênteses ou ponto final, ou reescreva
   a frase. Admite-se no máximo um travessão aposto por página, e só se nenhuma outra pontuação servir. Em títulos
   e tabelas, use hífen ou dois-pontos ("Livraria: e-commerce de livros").
2. **Listas onde cabe um parágrafo.** Bullets só para itens realmente paralelos e enumeráveis (entregáveis, marcos,
   premissas independentes). Raciocínio, justificativa, descrição e narrativa vão em prosa. Se cada bullet tem uma
   frase longa com "e", provavelmente é um parágrafo. Não use bullets com rótulo em negrito seguido de dois-pontos
   (`- **Segurança**: ...`) como padrão; prefira parágrafos curtos ou uma tabela.
3. **Negrito espalhado.** Negrito só em termos que o leitor precisa achar de relance; nunca vários por parágrafo.
4. **Tríades e paralelismos forçados.** "seguro, flexível e eficiente", "não apenas X, mas também Y", "do A ao B".
   Diga o que é específico em vez de enumerar três qualidades.
5. **Vocabulário inflado e genérico.** Evite: robusto, abrangente, crucial, fundamental, essencial, sem fricção,
   de ponta, otimizar, alavancar, garantir (quando não há garantia), "desempenha um papel", "nesse contexto",
   "vale ressaltar", "é importante destacar", "em suma", "por fim, mas não menos importante".
6. **Frases-resumo no fim de seção.** Não feche seções com um parágrafo que repete o que acabou de ser dito
   ("Dessas forças, ...", "Assim, o sistema garante..."). Termine no último fato relevante.
7. **Anúncios do que vem a seguir.** Corte "Esta seção descreve...", "A seguir apresentam-se..." quando o título já
   diz isso. Uma frase de introdução só se acrescentar algo.
8. **Excesso de referências entre parênteses.** Uma sequência de IDs `(RF-VEN-001, RF-VEN-002, RN-VEN-001, ...)` em
   toda frase fica mecânica. Cite os IDs essenciais, agrupe intervalos ("RF-VEN-011 a RF-VEN-015") e, quando houver
   muitos, mova-os para uma tabela ou para o fim do parágrafo.
9. **Ritmo uniforme.** Frases todas do mesmo tamanho e estrutura denunciam geração automática. Alterne frases curtas
   e médias; comece parágrafos de formas diferentes.
10. **Formatação de código no texto corrido.** Crase/código só para identificadores que o leitor vai procurar no
    sistema (status como EM TRANSPORTE podem ficar em maiúsculas sem crase). Em .docx, crases aparecem literais.
11. **Tom publicitário ou hedging excessivo.** Nada de promessas ("transforma a experiência"); nada de "pode
    potencialmente ajudar a". Afirme o que o sistema faz.
12. **Aspas e símbolos tipográficos de IA.** Setas (→) em prosa, emojis, "✅", reticências decorativas. Em prosa,
    escreva "passa de EM PROCESSAMENTO para APROVADA".

## Como revisar

1. Rode a busca de marcas e anote as ocorrências:
   ```bash
   grep -nE '—|–|→|\*\*[^*]+\*\*:|robust|abrangent|crucial|fundamental|essencial|sem fricção|vale ressaltar|é importante|em suma|nesse contexto|desempenha' <arquivos>
   grep -cE '^\s*[-*] ' <arquivos>   # quantidade de bullets por arquivo
   ```
2. Releia cada seção perguntando: um aluno escreveria assim? Converta listas em prosa onde for narrativa, troque
   travessões, corte frases-resumo e anúncios.
3. Preserve o conteúdo: fatos, IDs, números, nomes e a estrutura de títulos exigida pelo template não mudam. A
   revisão é de forma, não de substância.
4. Confira de novo com o grep; travessões devem ficar em zero ou perto disso.

## Exemplo

Antes:

> O benefício esperado é uma conclusão de compra segura e sem fricção — estoque verdadeiro garantido pelas reservas,
> pagamento flexível e pós-venda com troca simplificada — reduzindo erros manuais.

Depois:

> Com o sistema, o cliente não perde o livro que colocou no carrinho enquanto decide, pode dividir o pagamento entre
> cartões e cupons e pede a troca pelo próprio site. A loja deixa de controlar reservas e trocas à mão.
