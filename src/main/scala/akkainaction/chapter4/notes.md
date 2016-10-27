#Fault Tolerance

Available Strategies:
 - **Fault Containment Isolation**
   - keep fault isolated in component
 - **Structure**
   - keep faulty components from messing everything else up
 - **Redundancy**
   - Backup
 - **Replacement**
    - Seamlessly replace faulty component
 - **Reboot**
   - Reboot single component in bad state
 - **Component Lifecycle**
   - Faulty component should be able to go back to good state. Need defined lifecycle to do so (start, restart, terminate, etc..)
 - **Suspend**
   - Suspend calls to failed components so nothing is missed. Even failed call should not be missed
 - **Separation of concerns**
   - Separate recovery code from processing code

Supervision Strategies
 - User Guardian - restart on any exception (*default strategy*)
 - StoppingStrategy
 - BackOffSupervisor - Restarts child actors with a delay using a back-off algorithm when the child actor stops