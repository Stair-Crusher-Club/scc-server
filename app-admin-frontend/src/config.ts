export enum DeployEnvironmentType {
    LOCAL = 'LOCAL',
    DEV = 'DEV',
    PROD = 'PROD',
}

export let deployEnvironment: DeployEnvironmentType = DeployEnvironmentType.LOCAL;
const rawDeployEnvironment = process.env.REACT_APP_DEPLOY_ENVIRONMENT || 'LOCAL';
switch (rawDeployEnvironment) {
    case 'LOCAL': {
        deployEnvironment = DeployEnvironmentType.LOCAL;
        break;
    }
    case 'DEV': {
        deployEnvironment = DeployEnvironmentType.DEV;
        break;
    }
    case 'PROD': {
        deployEnvironment = DeployEnvironmentType.PROD;
        break;
    }
    default: {
        throw Error(`Invalid input for deployEnvironment: ${rawDeployEnvironment}`)
    }
}
