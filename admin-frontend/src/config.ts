export enum DeployEnvironmentType {
    LOCAL = 'LOCAL',
    DEV = 'DEV',
    PROD = 'PROD',
}

export let deployEnvironment: DeployEnvironmentType = DeployEnvironmentType.LOCAL;
const rawDeployEnvironment = process.env.DEPLOY_ENVIRONMENT || 'LOCAL';
switch(rawDeployEnvironment) {
    case 'LOCAL': {
        deployEnvironment = DeployEnvironmentType.LOCAL;
        break;
    }
    case 'DEV': {
        deployEnvironment = DeployEnvironmentType.LOCAL;
        break;
    }
    case 'PROD': {
        deployEnvironment = DeployEnvironmentType.LOCAL;
        break;
    }
    default: {
        throw Error(`Invalid input for deployEnvironment: ${rawDeployEnvironment}`)
    }
}
