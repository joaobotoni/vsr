select cron.schedule('limpar-sessoes-expiradas', '0 3 * * *', 'call rotinas.limpar_sessoes_expiradas()');
