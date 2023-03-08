/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.math.transform;

import com.sk89q.worldedit.math.Vector3;

/**
 * A more light-weight {@link Transform} than {@link AffineTransform}, supporting only translation and scaling.
 */
public class SimpleTransform implements Transform {
    public final Vector3 offset;
    public final Vector3 scale;

    public SimpleTransform(Vector3 offset, Vector3 scale) {
        this.offset = offset;
        this.scale = scale;
    }

    @Override
    public boolean isIdentity() {
        return offset.equals(Vector3.ZERO) && scale.equals(Vector3.ONE);
    }

    @Override
    public Vector3 apply(Vector3 input) {
        return input.multiply(scale).add(offset);
    }

    @Override
    public Transform inverse() {
        return new Transform() {
            @Override
            public boolean isIdentity() {
                return SimpleTransform.this.isIdentity();
            }

            @Override
            public Vector3 apply(Vector3 input) {
                return input.subtract(offset).divide(scale);
            }

            @Override
            public Transform inverse() {
                return SimpleTransform.this;
            }

            @Override
            public Transform combine(Transform other) {
                return new CombinedTransform(this, other);
            }
        };
    }

    @Override
    public Transform combine(Transform other) {
        return new CombinedTransform(this, other);
    }
}
