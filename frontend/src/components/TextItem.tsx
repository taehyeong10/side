import { TextWithPermission } from '../types';

interface Props {
  text: TextWithPermission;
  onEdit: (text: TextWithPermission) => void;
  onDelete: (id: string) => void;
}

function TextItem({ text, onEdit, onDelete }: Props) {
  const date = new Date(text.createdAt).toLocaleString();

  return (
    <div style={styles.card}>
      <div style={styles.content}>
        <div style={styles.meta}>
          <span style={styles.date}>{date}</span>
          {text.isCreator && <span style={styles.creatorBadge}>Creator</span>}
          <span style={styles.memberId}>Member #{text.memberId}</span>
        </div>
        <p style={styles.text}>{text.text}</p>
      </div>
      <div style={styles.actions}>
        {text.canEdit && (
          <button onClick={() => onEdit(text)} style={styles.editButton}>
            Edit
          </button>
        )}
        {text.canDelete && (
          <button onClick={() => onDelete(text.id)} style={styles.deleteButton}>
            Delete
          </button>
        )}
      </div>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  card: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    padding: 16,
    border: '1px solid #e0e0e0',
    borderRadius: 8,
    backgroundColor: '#fafafa',
  },
  content: {
    flex: 1,
    marginRight: 16,
  },
  meta: {
    display: 'flex',
    alignItems: 'center',
    gap: 8,
    marginBottom: 8,
    fontSize: 12,
    color: '#888',
  },
  date: {},
  creatorBadge: {
    padding: '2px 6px',
    borderRadius: 4,
    backgroundColor: '#e3f2fd',
    color: '#1565c0',
    fontWeight: 600,
    fontSize: 11,
  },
  memberId: {},
  text: {
    margin: 0,
    fontSize: 15,
    lineHeight: 1.5,
    whiteSpace: 'pre-wrap',
    wordBreak: 'break-word',
  },
  actions: {
    display: 'flex',
    gap: 8,
    flexShrink: 0,
  },
  editButton: {
    padding: '6px 12px',
    border: '1px solid #1976d2',
    borderRadius: 4,
    backgroundColor: '#fff',
    color: '#1976d2',
    cursor: 'pointer',
    fontSize: 13,
  },
  deleteButton: {
    padding: '6px 12px',
    border: '1px solid #d32f2f',
    borderRadius: 4,
    backgroundColor: '#fff',
    color: '#d32f2f',
    cursor: 'pointer',
    fontSize: 13,
  },
};

export default TextItem;
