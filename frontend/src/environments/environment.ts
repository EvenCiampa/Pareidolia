export const environment = {
  production: false, //verrà utilizzata nell'ambiente di sviluppo
  apiUrl: 'http://192.168.1.15:8080', //per le chiamate API generiche
  genericAccessEndpoint: 'http://192.168.1.15:8080/generic/access', //specifico per autenticazione
  consumerEndpoint: 'http://192.168.1.15:8080/consumer', //specifico per autenticazione
  promoterEndpoint: 'http://192.168.1.15:8080/promoter', //specifico per autenticazione
  adminEndpoint: 'http://192.168.1.15:8080/admin', //specifico per autenticazione
};
