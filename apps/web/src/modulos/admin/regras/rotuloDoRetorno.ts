/**
 * Rótulo do link de retorno do detalhe do pedido, de acordo com a consulta de origem. O mesmo detalhe é aberto a
 * partir dos pedidos (RF-VEN-009), das trocas (RF-VEN-013) e do cadastro do cliente (RF-CLI-005).
 */
export function rotuloDoRetorno(urlDeVolta: string): string {
  if (urlDeVolta.startsWith('/admin/trocas')) {
    return '← Voltar para as trocas';
  }
  if (urlDeVolta.startsWith('/admin/clientes/')) {
    return '← Voltar para o cliente';
  }
  if (urlDeVolta.startsWith('/admin/pedidos')) {
    return '← Voltar para os pedidos';
  }
  return '← Voltar';
}
