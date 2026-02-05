// utils/domHelpers.js
export class DOMHelper {
    static createElement(tag, attributes = {}, children = []) {
        const element = document.createElement(tag);

        Object.entries(attributes).forEach(([key, value]) => {
            if (key === 'className') {
                element.className = value;
            } else if (key === 'textContent') {
                element.textContent = value;
            } else if (key.startsWith('on')) {
                element.addEventListener(key.substring(2).toLowerCase(), value);
            } else {
                element.setAttribute(key, value);
            }
        });

        children.forEach(child => {
            if (typeof child === 'string') {
                element.appendChild(document.createTextNode(child));
            } else {
                element.appendChild(child);
            }
        });

        return element;
    }

    static batchUpdate(container, elements) {
        // Use DocumentFragment for batch DOM updates
        const fragment = document.createDocumentFragment();
        elements.forEach(el => fragment.appendChild(el));

        // Clear and append in one operation
        container.innerHTML = '';
        container.appendChild(fragment);
    }
}