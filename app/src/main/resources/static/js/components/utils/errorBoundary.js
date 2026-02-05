// utils/errorBoundary.js
export class ErrorBoundary {
    static wrapComponent(component, fallbackUI) {
        return (...args) => {
            try {
                return component(...args);
            } catch (error) {
                console.error('Component error:', error);

                // Send to error tracking service
                if (window.errorTrackingService) {
                    window.errorTrackingService.captureException(error);
                }

                return fallbackUI || `<div class="error-boundary">Something went wrong</div>`;
            }
        };
    }
}