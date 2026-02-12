import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
  url: 'http://localhost:8180',
  realm: 'side-realm',
  clientId: 'side-client',
});

export default keycloak;
