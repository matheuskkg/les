package br.fatec.les.livraria.notificacoes.infraestrutura.sse;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita o agendamento das tarefas do módulo, começando pelo heartbeat dos canais SSE (DEC-13). O agendamento fica no
 * {@code server} e não depende do perfil.
 */
@Configuration
@EnableScheduling
public class ConfiguracaoDeEventos {}
