/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package saker.std.main.environment.qualifier;

import saker.build.task.TaskContext;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.std.api.environment.qualifier.EnvironmentQualifier;

@NestInformation("Specifies a qualifier that determines a suitable build environment.\n"
		+ "Outputs from std.env.qualifier.*() tasks can be passed to it as an input, or values retrieved using other means.")
public interface EnvironmentQualifierTaskOption {
	public EnvironmentQualifierTaskOption clone();

	public void accept(Visitor visitor);

	public static EnvironmentQualifierTaskOption valueOf(EnvironmentQualifier qualifier) {
		return new SimpleEnvironmentQualifierTaskOption(qualifier);
	}

	public interface Visitor {
		public void visit(EnvironmentQualifier qualifier);
	}
}
