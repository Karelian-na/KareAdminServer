package cn.karelian.kas.utils;

public final class NonEmptyStrategy {
	public static final int NONE = 0x0000;
	public static final int QUERY = 0x0001;
	public static final int ADD = 0x0002;
	public static final int EDIT = 0x0004;
	public static final int DELETE = 0x0008;

	public static final int ADDEDIT = ADD | EDIT;
	public static final int OPERATION = ADD | EDIT | DELETE;
	public static final int ALL = 0x007F;
}