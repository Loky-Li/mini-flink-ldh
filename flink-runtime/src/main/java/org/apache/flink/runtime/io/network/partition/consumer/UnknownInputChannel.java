/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.runtime.io.network.partition.consumer;

import org.apache.flink.runtime.event.TaskEvent;
import org.apache.flink.runtime.io.network.ConnectionManager;
import org.apache.flink.runtime.io.network.TaskEventPublisher;
import org.apache.flink.runtime.io.network.partition.ResultPartitionID;
import org.apache.flink.runtime.io.network.partition.ResultPartitionManager;

import java.io.IOException;
import java.util.Optional;

import static org.apache.flink.util.Preconditions.checkNotNull;


class UnknownInputChannel extends InputChannel {

	private final ResultPartitionManager partitionManager;

	private final TaskEventPublisher taskEventPublisher;

	private final ConnectionManager connectionManager;

	/** Initial and maximum backoff (in ms) after failed partition requests. */
	private final int initialBackoff;

	private final int maxBackoff;

	public UnknownInputChannel(
			SingleInputGate gate,
			int channelIndex,
			ResultPartitionID partitionId,
			ResultPartitionManager partitionManager,
			TaskEventPublisher taskEventPublisher,
			ConnectionManager connectionManager,
			int initialBackoff,
			int maxBackoff) {

		super(gate, channelIndex, partitionId, initialBackoff, maxBackoff);

		this.partitionManager = checkNotNull(partitionManager);
		this.taskEventPublisher = checkNotNull(taskEventPublisher);
		this.connectionManager = checkNotNull(connectionManager);
		this.initialBackoff = initialBackoff;
		this.maxBackoff = maxBackoff;
	}

	@Override
	public void resumeConsumption() {
		throw new UnsupportedOperationException("UnknownInputChannel should never be blocked.");
	}

	@Override
	public void requestSubpartition(int subpartitionIndex) throws IOException {
		// Nothing to do here
	}

	@Override
	public Optional<BufferAndAvailability> getNextBuffer() throws IOException {
		// Nothing to do here
		throw new UnsupportedOperationException("Cannot retrieve a buffer from an UnknownInputChannel");
	}

	@Override
	public void sendTaskEvent(TaskEvent event) throws IOException {
		// Nothing to do here
	}

	/**
	 * Returns <code>false</code>.
	 *
	 * <p><strong>Important</strong>: It is important that the method correctly
	 * always <code>false</code> for unknown input channels in order to not
	 * finish the consumption of an intermediate result partition early.
	 */
	@Override
	public boolean isReleased() {
		return false;
	}

	@Override
	public void releaseAllResources() throws IOException {
		// Nothing to do here
	}

	@Override
	public String toString() {
		return "UnknownInputChannel [" + partitionId + "]";
	}

	// ------------------------------------------------------------------------
	// Graduation to a local or remote input channel at runtime
	// ------------------------------------------------------------------------

	public LocalInputChannel toLocalInputChannel() {
		return new LocalInputChannel(
			inputGate,
			getChannelIndex(),
			partitionId,
			partitionManager,
			taskEventPublisher,
			initialBackoff,
			maxBackoff);
	}
}
