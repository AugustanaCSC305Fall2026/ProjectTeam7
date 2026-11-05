import { useState } from "react";
import "./MessagingUI.css";

function ContactPicker({ contacts, value, onChange, disabled = false, id = "contact-picker", multiple = false }) {
  const [query, setQuery] = useState("");
  const [open, setOpen] = useState(false);
  const selected = contacts.find((contact) => contact.id === value);
  const selectedContacts = multiple
    ? contacts.filter((contact) => value.includes(contact.id))
    : [];
  const filtered = contacts.filter((contact) => {
    const text = `${contact.name} ${contact.email || ""} ${contact.relationship || ""}`.toLowerCase();
    return text.includes(query.toLowerCase());
  });

  function choose(contact) {
    if (multiple) {
      const nextValue = value.includes(contact.id)
        ? value.filter((id) => id !== contact.id)
        : [...value, contact.id];
      onChange(nextValue);
      setQuery("");
      setOpen(true);
      return;
    }
    onChange(contact.id);
    setQuery(contact.name);
    setOpen(false);
  }

  function remove(contactId) {
    onChange(value.filter((id) => id !== contactId));
  }

  function handleBlur() {
    window.setTimeout(() => setOpen(false), 120);
  }

  return (
    <div className="messaging-ui__picker">
      <div className={`messaging-ui__picker-input ${open ? "open" : ""} ${multiple ? "multiple" : ""}`}>
        {multiple ? selectedContacts.map((contact) => (
          <button className="messaging-ui__picker-chip" type="button" key={contact.id} onMouseDown={(event) => event.preventDefault()} onClick={() => remove(contact.id)}>
            {contact.name}<span aria-hidden="true">×</span>
          </button>
        )) : selected && <span className="messaging-ui__avatar messaging-ui__avatar--small" aria-hidden="true">{selected.name.charAt(0).toUpperCase()}</span>}
        <input
          id={id}
          role="combobox"
          aria-expanded={open}
          aria-controls={`${id}-options`}
          aria-autocomplete="list"
          value={multiple ? query : query}
          disabled={disabled}
          onFocus={() => setOpen(true)}
          onBlur={handleBlur}
          onChange={(event) => { setQuery(event.target.value); setOpen(true); if (!multiple) onChange(""); }}
          placeholder={multiple && selectedContacts.length ? "Add another person..." : "Search people..."}
        />
        <span aria-hidden="true">⌄</span>
      </div>
      {open && !disabled && (
        <div className="messaging-ui__picker-options" id={`${id}-options`} role="listbox">
          {filtered.length === 0 ? (
            <p className="messaging-ui__picker-empty">No matching people found.</p>
          ) : filtered.map((contact) => (
            <button type="button" role="option" aria-selected={multiple ? value.includes(contact.id) : contact.id === value} key={contact.id} onClick={() => choose(contact)}>
              <span className="messaging-ui__avatar messaging-ui__avatar--small" aria-hidden="true">{contact.name.charAt(0).toUpperCase()}</span>
              <span className="messaging-ui__picker-copy"><strong>{contact.name}</strong><small>{contact.relationship || "Contact"}{contact.email ? ` · ${contact.email}` : ""}</small></span>
              {multiple && value.includes(contact.id) && <span className="messaging-ui__picker-check" aria-label="Selected">✓</span>}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}

export default ContactPicker;
