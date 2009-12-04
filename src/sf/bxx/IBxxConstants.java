/*
 * Copyright (c) 2008, Chris Leung, simontsui. All rights reserved.
 *
 * The software in this package is distributed under the GNU General Public
 * License version 2, as published by the Free Software Foundation, but with
 * the Classpath exception.  You should have received a copy of the GNU General
 * Public License (GPL) and the Classpath exception along with this program.
 */
package sf.bxx;

public interface IBxxConstants {

	byte[] MAGIC = { (byte)0xbf, 'b', 'x', 'x' };
	int MAJOR = 2;
	int MINOR = 0;
	byte[] NO_BYTES = new byte[0];
	byte[] HEADER_PADDING = {
		0, 0, 0, 0, 0, 0, 0, 0, //
		0, 0, 0, 0, 0, 0, 0, 0,
	};
	char DECL_PREFIX = '#';

	long FlagTagDict = 0x01;
	long FlagAttrDict = 0x02;

	int LSB03 = 0x00000007;
	int LSB04 = 0x0000000f;
	int LSB06 = 0x0000003f;
	int LSB07 = 0x0000007f;
	int LSB11 = 0x000007ff;
	int LSB12 = 0x00000fff;
	int LSB13 = 0x00001fff;
	int LSB14 = 0x00003fff;
	int LSB15 = 0x00007fff;
	int LSB19 = 0x0007ffff;
	int LSB20 = 0x000fffff;
	int LSB21 = 0x001fffff;
	int LSB27 = 0x07ffffff;
	int LSB28 = 0x0fffffff;
	int LSB29 = 0x1fffffff;
	int LSB30 = 0x3fffffff;
	int LSB31 = 0x7fffffff;
	int LSB32 = 0xffffffff;

	long LSB28L = 0x000000000fffffffL;
	long LSB29L = 0x000000001fffffffL;
	long LSB30L = 0x000000003fffffffL;
	long LSB31L = 0x000000007fffffffL;
	long LSB32L = 0x00000000ffffffffL;
	long LSB36L = 0x0000000fffffffffL;
	long LSB40L = 0x000000ffffffffffL;
	long LSB44L = 0x00000fffffffffffL;
	long LSB48L = 0x0000ffffffffffffL;
	long LSB52L = 0x000fffffffffffffL;
	long LSB56L = 0x00ffffffffffffffL;
	long LSB60L = 0x0fffffffffffffffL;
	long LSB64L = 0xffffffffffffffffL;

	int SigNONE = 0x00;
	int SigMD5 = 0x01;
	int SigSHA1 = 0x02;
	int SigSHA256 = 0x03;
	int SigPGP = 0x10;

	int NullValue = 0xf0;

	int TagInvalid = 0x0;
	int TagExtension = 0xffffffff;
	int TagTrailer = 0xfffffffe;
	int TagDoctype = 0xfffffffd;
	int TagDeclaration = 0xfffffffc;
	int TagPI = 0xfffffffb;
	int TagComment = 0xfffffffa;
	int TagCDATA = 0xfffffff9;
	int TagF8 = 0xfffffff8;
	int TagEndAttr = 0xfffffff7;
	int TagEndTag = 0xfffffff6;
	int TagBlobDef = 0xfffffff5;
	int TagStringDef = 0xfffffff4;
	int TagBlobRef = 0xfffffff3;
	int TagStringRef = 0xfffffff2;
	int TagBlob = 0xfffffff1;
	int TagString = 0xfffffff0;
	//
	int TagChar = 0xffffffef;
	int TagDouble = 0xffffffee;
	int TagFloat = 0xffffffed;
	int TagBigInt = 0xffffffec;
	int TagLong = 0xffffffeb;
	int TagInt = 0xffffffea;
	int TagShort = 0xffffffe9;
	int TagByte = 0xffffffe8;
	int TagULong = 0xffffffe7;
	int TagUInt = 0xffffffe6;
	int TagUShort = 0xffffffe5;
	int TagUByte = 0xffffffe4;
	int TagTrue = 0xffffffe3;
	int TagFalse = 0xffffffe2;
	int TagArray = 0xffffffe1;
	int TagAbsent = 0xffffffe0;
	//
	String[] TagNames = {
		"<invalid>",		
		"<extension>",	
		"<trailer>",
		"<doctype>",
		"<declaration>",
		"<pi>",
		"<comment>",
		"<cdata>",
		"<0xf8>",
		"<endattr>",
		"<endtag>",
		"<blobdef>",
		"<stringdef>",
		"<blobref>",
		"<stringref>",
		"<blob>",
		"<string>",
		//
		"<char>",
		"<double>",
		"<float>",
		"<bigInt>",
		"<long>",
		"<int>",
		"<short>",
		"<byte>",
		"<ulong>",
		"<uint>",
		"<ushort>",
		"<ubyte>",
		"<true>",
		"<false>",
		"<array>",
		"<absent>", 
	};

	int NULL = 0;
	int EMPTY_STRING = NULL + 1;
	int XML = EMPTY_STRING + 1;
	int VERSION = XML + 1;
	int ENCODING = VERSION + 1;
	int STANDALONE = ENCODING + 1;
	int VER10 = STANDALONE + 1;
	int UTF8 = VER10 + 1;
	int YES = UTF8 + 1;
	int NO = YES + 1;
	int DOCTYPE = NO + 1;
	int PUBLIC = DOCTYPE + 1;
	int SYSTEM = PUBLIC + 1;
	int DTD = SYSTEM + 1;
	int DATA = DTD + 1;
	//
	int StandardTagStart = 0;
	int StandardTagEnd = DATA + 1;
	//
	String[] StandardTagStrings = {
		null,			
		"",
		"xml",
		"version",
		"encoding",
		"standalone",
		"1.0",
		"UTF-8",
		"yes",		
		"no",
		"DOCTYPE",
		"#PUBLIC",
		"#SYSTEM",
		"#DTD",
		"#DATA",
	};

	int EMPTY_BLOB = 1;
}
