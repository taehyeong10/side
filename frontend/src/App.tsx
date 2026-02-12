import { useEffect, useState } from 'react';
import keycloak from './keycloak';
import TextList from './components/TextList';

function App() {
  const [authenticated, setAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    keycloak
      .init({ onLoad: 'login-required', checkLoginIframe: false })
      .then((auth) => {
        setAuthenticated(auth);
        setLoading(false);

        // Auto-refresh token
        setInterval(() => {
          keycloak.updateToken(60).catch(() => keycloak.login());
        }, 30000);
      })
      .catch(() => {
        setLoading(false);
      });
  }, []);

  if (loading) {
    return (
      <div style={styles.center}>
        <p>Loading...</p>
      </div>
    );
  }

  if (!authenticated) {
    return (
      <div style={styles.center}>
        <p>Authentication failed.</p>
        <button onClick={() => keycloak.login()} style={styles.button}>
          Login
        </button>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <header style={styles.header}>
        <h1 style={styles.title}>Text Management</h1>
        <div style={styles.userInfo}>
          <span>{keycloak.tokenParsed?.preferred_username}</span>
          <button onClick={() => keycloak.logout()} style={styles.logoutButton}>
            Logout
          </button>
        </div>
      </header>
      <main style={styles.main}>
        <TextList />
      </main>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  center: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    height: '100vh',
    fontFamily: 'system-ui, sans-serif',
  },
  container: {
    maxWidth: 900,
    margin: '0 auto',
    padding: '0 20px',
    fontFamily: 'system-ui, sans-serif',
  },
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: '16px 0',
    borderBottom: '1px solid #e0e0e0',
  },
  title: {
    margin: 0,
    fontSize: 24,
  },
  userInfo: {
    display: 'flex',
    alignItems: 'center',
    gap: 12,
  },
  main: {
    padding: '20px 0',
  },
  button: {
    padding: '8px 16px',
    border: 'none',
    borderRadius: 4,
    backgroundColor: '#1976d2',
    color: '#fff',
    cursor: 'pointer',
    fontSize: 14,
  },
  logoutButton: {
    padding: '6px 12px',
    border: '1px solid #ccc',
    borderRadius: 4,
    backgroundColor: '#fff',
    cursor: 'pointer',
    fontSize: 13,
  },
};

export default App;
