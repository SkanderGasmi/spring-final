// components/modals/lazyLoader.js
export class ModalLazyLoader {
    static async loadModal(name) {
        const loaders = {
            patientLogin: () => import('./forms/LoginForm.js').then(m => m.createLoginForm('patient')),
            patientSignup: () => import('./forms/patient/PatientSignup.js'),
            addDoctor: () => import('./forms/admin/AddDoctorForm.js')
        };

        const loader = loaders[name];
        if (!loader) {
            throw new Error(`Modal "${name}" not found`);
        }

        return loader();
    }
}