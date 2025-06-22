export const environment = {
  production: false, //verrà utilizzata nell'ambiente di sviluppo
  apiUrl: 'http://localhost:8080', //per le chiamate API generiche
  genericAccessEndpoint: 'http://localhost:8080/generic/access', //specifico per autenticazione
  consumerEndpoint: 'http://localhost:8080/consumer', //specifico per autenticazione
  promoterEndpoint: 'http://localhost:8080/promoter', //specifico per autenticazione
  adminEndpoint: 'http://localhost:8080/admin', //specifico per autenticazione
};
