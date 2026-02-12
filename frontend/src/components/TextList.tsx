import { useEffect, useState, useCallback } from 'react';
import api from '../api';
import { TextWithPermission } from '../types';
import TextItem from './TextItem';
import TextCreateForm from './TextCreateForm';
import TextEditModal from './TextEditModal';

function TextList() {
  const [texts, setTexts] = useState<TextWithPermission[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [editingText, setEditingText] = useState<TextWithPermission | null>(null);

  const fetchTexts = useCallback(async () => {
    try {
      setError(null);
      const response = await api.get<TextWithPermission[]>('/texts/readable');
      setTexts(response.data);
    } catch (err) {
      setError('Failed to load texts.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchTexts();
  }, [fetchTexts]);

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to delete this text?')) return;
    try {
      await api.delete(`/texts/${id}`);
      await fetchTexts();
    } catch (err) {
      console.error('Failed to delete:', err);
      alert('Failed to delete text.');
    }
  };

  const handleEdit = (text: TextWithPermission) => {
    setEditingText(text);
  };

  const handleEditSave = async (id: string, newText: string) => {
    try {
      await api.put(`/texts/${id}`, { text: newText });
      setEditingText(null);
      await fetchTexts();
    } catch (err) {
      console.error('Failed to update:', err);
      alert('Failed to update text.');
    }
  };

  const handleCreate = async (text: string) => {
    try {
      await api.post('/texts', { text });
      setShowCreateForm(false);
      await fetchTexts();
    } catch (err) {
      console.error('Failed to create:', err);
      alert('Failed to create text.');
    }
  };

  if (loading) return <p>Loading texts...</p>;
  if (error) return <p style={{ color: 'red' }}>{error}</p>;

  return (
    <div>
      <div style={styles.toolbar}>
        <h2 style={styles.heading}>Your Texts</h2>
        <button onClick={() => setShowCreateForm(true)} style={styles.createButton}>
          + New Text
        </button>
      </div>

      {showCreateForm && (
        <TextCreateForm
          onSave={handleCreate}
          onCancel={() => setShowCreateForm(false)}
        />
      )}

      {editingText && (
        <TextEditModal
          text={editingText}
          onSave={handleEditSave}
          onCancel={() => setEditingText(null)}
        />
      )}

      {texts.length === 0 ? (
        <p style={styles.empty}>No texts available.</p>
      ) : (
        <div style={styles.list}>
          {texts.map((text) => (
            <TextItem
              key={text.id}
              text={text}
              onEdit={handleEdit}
              onDelete={handleDelete}
            />
          ))}
        </div>
      )}
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  toolbar: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  heading: {
    margin: 0,
    fontSize: 20,
  },
  createButton: {
    padding: '8px 16px',
    border: 'none',
    borderRadius: 4,
    backgroundColor: '#1976d2',
    color: '#fff',
    cursor: 'pointer',
    fontSize: 14,
  },
  list: {
    display: 'flex',
    flexDirection: 'column',
    gap: 12,
  },
  empty: {
    color: '#888',
    textAlign: 'center',
    padding: 40,
  },
};

export default TextList;
