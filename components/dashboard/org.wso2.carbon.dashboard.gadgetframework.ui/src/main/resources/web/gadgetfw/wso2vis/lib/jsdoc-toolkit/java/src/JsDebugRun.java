/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
/**
 * A trivial bootstrap class that simply adds the path to the
 * .js file as an argument to the Rhino call. This little hack
 * allows the code in the .js file to have access to it's own 
 * path via the Rhino arguments object. This is necessary to 
 * allow the .js code to find resource files in a location 
 * relative to itself.
 *
 * USAGE: java -jar jsdebug.jar path/to/file.js
 */
public class JsDebugRun {
	public static void main(String[] args) {
		String[] jsargs = {"-j="+args[0]};
		
		String[] allArgs = new String[jsargs.length + args.length];
		System.arraycopy(args, 0, allArgs, 0, args.length);
		System.arraycopy(jsargs, 0, allArgs, args.length ,jsargs.length);

		org.mozilla.javascript.tools.debugger.Main.main(allArgs);
    }
}
