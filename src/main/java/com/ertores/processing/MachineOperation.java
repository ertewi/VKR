package com.ertores.processing;

public enum MachineOperation {
	CRUSHING("crushing"),
	GRINDING("grinding"),
	ENRICHING("enriching"),
	SMELTING("smelting");

	private final String id;

	MachineOperation(String id) {
		this.id = id;
	}

	public String id() {
		return id;
	}

	public static MachineOperation byId(String id) {
		for (MachineOperation operation : values()) {
			if (operation.id.equals(id)) {
				return operation;
			}
		}
		throw new IllegalArgumentException("Unknown processing operation: " + id);
	}
}
