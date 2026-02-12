import { useState } from 'react';
import { TextWithPermission } from '../types';

interface Props {
  text: TextWithPermission;
  onSave: (id: string, newText: string) => void;
  onCancel: () => void;
}

function TextEditModal({ text, onSave, onCancel }: Props) {
  const [value, setValue] = useState(text.text);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!value.trim()) return;
    onSave(text.id, value.trim());
  };

  return (
    <div style={styles.overlay}>
      <form onSubmit={handleSubmit} style={styles.form}>
        <h3 style={styles.heading}>Edit Text</h3>
        <textarea
          value={value}
          onChange={(e) => setValue(e.target.value)}
          style={styles.textarea}
          rows={4}
          autoFocus
        />
        <div style={styles.buttons}>
          <button type="button" onClick={onCancel} style={styles.cancelButton}>
            Cancel
          </button>
          <button type="submit" disabled={!value.trim()} style={styles.saveButton}>
            Save
          </button>
        </div>
      </form>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  overlay: {
    position: 'fixed',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: 'rgba(0,0,0,0.4)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 1000,
  },
  form: {
    backgroundColor: '#fff',
    padding: 24,
    borderRadius: 8,
    width: '100%',
    maxWidth: 500,
    boxShadow: '0 4px 20px rgba(0,0,0,0.15)',
  },
  heading: {
    margin: '0 0 16px',
    fontSize: 18,
  },
  textarea: {
    width: '100%',
    padding: 10,
    border: '1px solid #ccc',
    borderRadius: 4,
    fontSize: 14,
    resize: 'vertical',
    fontFamily: 'inherit',
    boxSizing: 'border-box',
  },
  buttons: {
    display: 'flex',
    justifyContent: 'flex-end',
    gap: 8,
    marginTop: 12,
  },
  cancelButton: {
    padding: '8px 16px',
    border: '1px solid #ccc',
    borderRadius: 4,
    backgroundColor: '#fff',
    cursor: 'pointer',
    fontSize: 14,
  },
  saveButton: {
    padding: '8px 16px',
    border: 'none',
    borderRadius: 4,
    backgroundColor: '#1976d2',
    color: '#fff',
    cursor: 'pointer',
    fontSize: 14,
  },
};

export default TextEditModal;
