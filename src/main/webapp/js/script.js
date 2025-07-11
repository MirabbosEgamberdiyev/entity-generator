class EntityGenerator {
    static fieldTypes = [
        'String', 'Integer', 'Long', 'Double', 'Boolean', 'LocalDate', 'LocalDateTime'
    ];

    static relationshipTypes = [
        'OneToOne', 'OneToMany', 'ManyToOne', 'ManyToMany'
    ];

    static fetchTypes = ['LAZY', 'EAGER'];

    constructor() {
        this.baseUrl = window.location.origin.replace(':63342', ':8080');
        this.initEventListeners();
        this.updateSwaggerLink();
    }

    initEventListeners() {
        document.getElementById('entityForm').addEventListener('submit', this.handleSubmit.bind(this));
    }

    updateSwaggerLink() {
        const swaggerLink = document.getElementById('swaggerLink');
        swaggerLink.href = `${this.baseUrl}/swagger-ui/index.html`;
    }

    addField() {
        const container = document.getElementById('fieldsContainer');
        const fieldItem = document.createElement('div');
        fieldItem.className = 'field-item';
        fieldItem.innerHTML = `
      <div class="form-group">
        <label>Field Name</label>
        <input type="text" name="fieldName" required aria-required="true">
      </div>
      <div class="form-group">
        <label>Field Type</label>
        <select name="fieldType" aria-label="Field Type">
          ${EntityGenerator.fieldTypes.map(type => `<option value="${type}">${type}</option>`).join('')}
        </select>
      </div>
      <div class="form-group checkbox-group">
        <input type="checkbox" name="notNull" id="notNull_${Date.now()}">
        <label for="notNull_${Date.now()}">Not Null</label>
      </div>
      <div class="form-group checkbox-group">
        <input type="checkbox" name="primaryKey" id="primaryKey_${Date.now()}">
        <label for="primaryKey_${Date.now()}">Primary Key</label>
      </div>
      <div class="form-group">
        <label>Min Size</label>
        <input type="number" name="minSize" min="0" aria-label="Minimum Size">
      </div>
      <div class="form-group">
        <label>Max Size</label>
        <input type="number" name="maxSize" min="0" aria-label="Maximum Size">
      </div>
      <div class="form-group">
        <label>Description</label>
        <input type="text" name="description" aria-label="Field Description">
      </div>
      <button type="button" class="button button--remove" onclick="this.parentElement.remove()">Remove</button>
    `;
        container.appendChild(fieldItem);
    }

    addRelationship() {
        const container = document.getElementById('relationshipsContainer');
        const relItem = document.createElement('div');
        relItem.className = 'relationship-item';
        relItem.innerHTML = `
      <div class="form-group">
        <label>Relationship Type</label>
        <select name="relationshipType" aria-label="Relationship Type">
          ${EntityGenerator.relationshipTypes.map(type => `<option value="${type}">${type}</option>`).join('')}
        </select>
      </div>
      <div class="form-group">
        <label>Target Entity</label>
        <input type="text" name="targetEntity" required aria-required="true">
      </div>
      <div class="form-group">
        <label>Source Field</label>
        <input type="text" name="sourceField" required aria-required="true">
      </div>
      <div class="form-group">
        <label>Fetch Type</label>
        <select name="fetchType" aria-label="Fetch Type">
          ${EntityGenerator.fetchTypes.map(type => `<option value="${type}">${type}</option>`).join('')}
        </select>
      </div>
      <button type="button" class="button button--remove" onclick="this.parentElement.remove()">Remove</button>
    `;
        container.appendChild(relItem);
    }

    async handleSubmit(e) {
        e.preventDefault();
        const entityName = document.getElementById('entityName').value.trim();
        const entityNameError = document.getElementById('entityNameError');

        if (!entityName) {
            this.showError(entityNameError, 'Entity name is required');
            return;
        }
        this.hideError(entityNameError);

        const fields = Array.from(document.querySelectorAll('.field-item')).map(item => {
            const minSize = item.querySelector('[name="minSize"]').value;
            const maxSize = item.querySelector('[name="maxSize"]').value;
            const validations = [];

            if (item.querySelector('[name="notNull"]').checked) {
                validations.push({ type: 'NotNull' });
            }
            if (minSize || maxSize) {
                const params = {};
                if (minSize) params.min = parseInt(minSize);
                if (maxSize) params.max = parseInt(maxSize);
                validations.push({ type: 'Size', parameters: params });
            }

            return {
                name: item.querySelector('[name="fieldName"]').value,
                type: item.querySelector('[name="fieldType"]').value,
                nullable: !item.querySelector('[name="notNull"]').checked,
                primaryKey: item.querySelector('[name="primaryKey"]').checked,
                validations,
                swaggerConfig: { description: item.querySelector('[name="description"]').value || null }
            };
        });

        const relationships = Array.from(document.querySelectorAll('.relationship-item')).map(item => ({
            type: item.querySelector('[name="relationshipType"]').value,
            targetEntity: item.querySelector('[name="targetEntity"]').value,
            sourceField: item.querySelector('[name="sourceField"]').value,
            fetch: item.querySelector('[name="fetchType"]').value
        }));

        if (fields.length === 0) {
            this.showAlert('At least one field is required');
            return;
        }

        const metadata = { entityName, fields, relationships };

        try {
            const response = await fetch(`${this.baseUrl}/api/generator/generate`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(metadata)
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            this.showAlert(result.success ? 'Entity generated successfully!' : `Error: ${result.message}`);

            if (result.success) {
                window.location.href = `${this.baseUrl}/swagger-ui/index.html`;
            }
        } catch (error) {
            this.showAlert(`Error generating entity: ${error.message}`);
        }
    }

    showError(element, message) {
        element.textContent = message;
        element.classList.add('error--visible');
    }

    hideError(element) {
        element.classList.remove('error--visible');
        element.textContent = '';
    }

    showAlert(message) {
        alert(message); // Consider replacing with a modern notification system
    }
}

// Initialize the generator
const generator = new EntityGenerator();
window.generator = generator;