package com.eriksonn.createaeronautics.physics.collision.resolution;

import com.eriksonn.createaeronautics.physics.AbstractContraptionRigidbody;
import com.eriksonn.createaeronautics.physics.IRigidbody;
import com.eriksonn.createaeronautics.physics.SimulatedContraptionRigidbody;
import com.eriksonn.createaeronautics.physics.collision.detection.Contact;
import com.eriksonn.createaeronautics.physics.collision.detection.Manifold;
import com.sun.org.apache.bcel.internal.generic.DCONST;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.vector.Vector3d;

import java.util.List;

import static com.eriksonn.createaeronautics.physics.collision.detection.GJKEPA.EPSILON;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Manifold solver making use of sequential impulse resolution.
 */
public class SequentialManifoldSolver implements IIterativeManifoldSolver {

    @Override
    public double getRestitution(BlockState blockA, BlockState blockB) {
        return 0.0;
    }

    @Override
    public void preSolve(List<Contact> contacts) {

        // Iterate over all contacts
        for (Contact contact : contacts) {

            // Inverse Mass
            double inverseMass = 1.0 / contact.rigidbody.getMass();
            contact.inverseMass = inverseMass;

            contact.normalImpulse = 0.0;
            contact.tangentImpulse = 0.0;

            contact.localContactPoint = contact.rigidbody.toLocal(contact.manifold.contactPointA);

            Vector3d localNormal = contact.rigidbody.rotateInverse(contact.manifold.normal);
            contact.normalMass = inverseMass + contact.rigidbody.multiplyInertiaInverse(contact.localContactPoint.cross(localNormal)).cross(contact.localContactPoint).dot(localNormal);
        }

    }

    @Override
    public void solve(IRigidbody rigidbody, List<Contact> contacts, double dt) {
        for (Contact contact : contacts) {
            Vector3d relativeVelocity = rigidbody.getVelocityAtPoint(contact.localContactPoint);

            // Relative velocity along the collision normal
            double velocityAlongCollisionNormal = relativeVelocity.dot(contact.manifold.normal.scale(-1.0));

            // Total restitution coefficient(commonly denoted as `e`)
            double e = getRestitution(contact.blockA, contact.blockB);

            // Impulse scalar
            double impulseDelta = (-(1.0 + e) * velocityAlongCollisionNormal) / (contact.normalMass);

            // Impulse clamping based on Erin Catto's GDC 2014 talk
            double newImpulse = Math.max(contact.normalImpulse + impulseDelta, 0);
            impulseDelta = newImpulse - contact.normalImpulse;
            contact.normalImpulse = newImpulse;

            // The impulse vector
            Vector3d impulse = contact.manifold.normal.scale(impulseDelta);

            // If the impulse isn't NaN
            if (!Double.isNaN(impulse.x)) {
                rigidbody.applyImpulse(contact.localContactPoint, impulse.scale(-1.0));

                relativeVelocity = rigidbody.getVelocityAtPoint(contact.localContactPoint);

                // Friction!
                // Get tangent vec
                Vector3d tangent = relativeVelocity.subtract(contact.manifold.normal.scale(relativeVelocity.dot(contact.manifold.normal))).normalize();

                // Friction vector magnitude, denoted as jt
                double jt = (-relativeVelocity.dot(tangent)) / contact.inverseMass;

                if (Math.abs(jt) < EPSILON)
                    return;

                double staticFriction = 0.8,
                        dynamicFriction = 0.7;

                // Friction impulse
                // Coulumb's law
                Vector3d tangentImpulse;
                if (Math.abs(jt) < impulseDelta * staticFriction)
                    tangentImpulse = tangent.scale(jt);
                else
                    tangentImpulse = tangent.scale(-impulseDelta).scale(dynamicFriction);

//             rigidbody.applyImpulse(veloPoint, tangentImpulse.scale(-1.0f));
            }
        }
    }


}
