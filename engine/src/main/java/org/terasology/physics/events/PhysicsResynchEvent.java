/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.physics.events;

import org.terasology.entitySystem.event.Event;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.BroadcastEvent;

/**
 */
@BroadcastEvent
public class PhysicsResynchEvent implements Event {
    private Vector3f position = new Vector3f();
    private Quat4f rotation = new Quat4f();
    private Vector3f velocity = new Vector3f();
    private Vector3f angularVelocity = new Vector3f();

    protected PhysicsResynchEvent() {
    }

    public PhysicsResynchEvent(Vector3f position, Quat4f rotation, Vector3f velocity, Vector3f angularVelocity) {
        this.position.set(position);
        this.rotation.set(rotation);
        this.velocity.set(velocity);
        this.angularVelocity.set(angularVelocity);
    }

    /**
     * @return The position of the physics entity when this event is sent. Copy to use.
     */
    public Vector3f getPosition() {
        return position;
    }

    /**
     * @return The rotation of the physics entity when this event is sent. Copy to use.
     */
    public Quat4f getRotation() {
        return rotation;
    }

    /**
     * @return the linear velocity of the physics entity when this event is sent. Copy to use.
     */
    public Vector3f getVelocity() {
        return velocity;
    }

    /**
     * @return The angular or rotational velocity of the physics entity when this event is sent. Copy to use.
     */
    public Vector3f getAngularVelocity() {
        return angularVelocity;
    }
}
