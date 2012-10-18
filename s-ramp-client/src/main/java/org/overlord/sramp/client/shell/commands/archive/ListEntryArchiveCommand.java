/*
 * Copyright 2012 JBoss Inc
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
package org.overlord.sramp.client.shell.commands.archive;

import java.util.List;

import javax.xml.namespace.QName;

import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.atom.archive.SrampArchiveEntry;
import org.overlord.sramp.client.shell.AbstractShellCommand;
import org.overlord.sramp.client.shell.ShellContext;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Property;
import org.s_ramp.xmlns._2010.s_ramp.Relationship;
import org.s_ramp.xmlns._2010.s_ramp.Target;

/**
 * Removes an entry from the current S-RAMP batch archive.
 *
 * @author eric.wittmann@redhat.com
 */
public class ListEntryArchiveCommand extends AbstractShellCommand {

	/**
	 * Constructor.
	 */
	public ListEntryArchiveCommand() {
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		System.out.println("archive:listEntry <archivePath>");
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#execute(org.overlord.sramp.client.shell.ShellContext)
	 */
	@Override
	public void execute(ShellContext context) throws Exception {
		String archivePathArg = requiredArgument(0, "Please include an entry path (relative archive path).");

		QName varName = new QName("archive", "active-archive");
		SrampArchive archive = (SrampArchive) context.getVariable(varName);

		if (archive == null) {
			System.out.println("No S-RAMP archive is currently open.");
		} else {
			SrampArchiveEntry entry = archive.getEntry(archivePathArg);
			BaseArtifactType metaData = entry.getMetaData();
			ArtifactType artifactType = ArtifactType.valueOf(metaData);
			System.out.println("Entry: " + archivePathArg);
			System.out.println("-----");
			printProperty("Type", artifactType.getArtifactType().getType());
			printProperty("Model", artifactType.getArtifactType().getModel());
			printProperty("UUID", metaData.getUuid());
			printProperty("Name", metaData.getName());
			printProperty("Version", metaData.getVersion());
			printProperty("Created By", metaData.getCreatedBy());
			if (metaData.getCreatedTimestamp() != null)
				printProperty("Created On", metaData.getCreatedTimestamp().toXMLFormat());
			printProperty("Modified By", metaData.getLastModifiedBy());
			if (metaData.getLastModifiedTimestamp() != null)
				printProperty("Modified On", metaData.getLastModifiedTimestamp().toXMLFormat());
			System.out.println("  -- Custom Properties --");
			for (Property property : metaData.getProperty()) {
				printProperty(property.getPropertyName(), property.getPropertyValue());
			}
			System.out.println("  -- Generic Relationships --");
			for (Relationship relationship : metaData.getRelationship()) {
				List<Target> targets = relationship.getRelationshipTarget();
				String targetStr = "";
				boolean first = true;
				for (Target target : targets) {
					if (!first) {
						targetStr += ", ";
					} else {
						first = false;
					}
					targetStr += target.getValue();
				}
				printProperty(relationship.getRelationshipType(), targetStr);
			}

//			printProperty("Description", metaData.getDescription());
		}
	}

	/**
	 * Prints out a single property from the s-ramp meta-data.
	 * @param propertyName
	 * @param propertyValue
	 */
	private void printProperty(String propertyName, String propertyValue) {
		if (propertyValue != null)
			System.out.printf("  %1$s: %2$s\n", propertyName, propertyValue);
	}

}
