import com.google.protobuf.Message;

	class DemoMessage{
		Message msg;
		//MetaData
		boolean compressed;
		int tick, size, uncompressed_size;

		public String msgType(){
			return msg.getClass().getSimpleName();
		}
	}