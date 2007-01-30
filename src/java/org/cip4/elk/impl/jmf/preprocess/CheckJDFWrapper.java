/*
 * Created on Jun 21, 2005
 */
package org.cip4.elk.impl.jmf.preprocess;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.CopyUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.cip4.jdflib.CheckJDF;
import org.cip4.jdflib.jmf.JDFJMF;
import org.cip4.jdflib.node.JDFNode;
import org.jdom.Document;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

/**
 * A wrapper for easy validation with CheckJDF.
 * 
 * The wrapper calls CheckJDF with the following command line arguments:
 * <ul>
 * <li>-q -- is quiet for valid files</li>
 * <li>-c -- requires all required elements and attributes to exist, else
 * incomplete JDF is OK</li>
 * <li>-v -- validate using XML Schema validation</li>
 * </ul>
 * 
 * @author Claes Buckwalter (clabu@itn.liu.se)
 */
public class CheckJDFWrapper {

	/**
	 * Prevents instances from being created.
	 */
	private CheckJDFWrapper() {
	}

	/**
	 * Validates the JDF instance or JMF message read from the
	 * <code>InputStream</code>
	 * 
	 * @param jdfIn
	 *            the JDF instance or JMF message to parse
	 * @param reportFile
	 *            the absolute path to the file to write the validation report
	 *            to
	 * @throws IOException
	 */
	public static void validate(InputStream jdfIn, File reportFile)
			throws IOException {
		// Write input stream to temp file
		File tempJdf = createTempFile(".jdf");
		try {
			FileOutputStream jdfOut = new FileOutputStream(tempJdf);
			CopyUtils.copy(jdfIn, jdfOut);
			System.out.println(tempJdf.getAbsolutePath());
			validate(tempJdf, reportFile);

		} catch (IOException ioe) {
			System.err.println(ioe);
		} finally {
			tempJdf.delete();
		}
	}

	/**
	 * Validates a JDF instance or JMF message read from the specified
	 * <code>String</code>
	 * 
	 * @param jdf
	 *            the JDF instance or JMF message to parse
	 * @return a String containing a validation report in XML format
	 * @throws IOException
	 */
	public static String validate(String jdf) throws IOException {
		File tempReport = createTempFile(".xml");

		try {
			validate(jdf, tempReport);
			return IOUtils.toString(new FileInputStream(tempReport));
		} finally {
			tempReport.delete();
		}
	}

	/**
	 * Validates the JDF instance or JMF message read from the
	 * <code>String</code>
	 * 
	 * @param jdf
	 *            the JDF instance or JMF message to parse
	 * @param reportFile
	 *            the absolute path to the file to write the validation report
	 *            to
	 * @throws IOException
	 */
	public static void validate(String jdf, File reportFile) throws IOException {
		// Write input stream to temp file
		File tempJdf = createTempFile(".jdf");
		try {
			FileOutputStream jdfOut = new FileOutputStream(tempJdf);

			CopyUtils.copy(jdf, jdfOut);
			validate(tempJdf, reportFile);
		} catch (IOException ioe) {
			System.err.println(ioe);
		} finally {
			tempJdf.delete();
		}
	}

	/**
	 * Validates the JDF instance or JMF message read from a file. A validation
	 * report in XML format is written to a file. Both schema valid
	 * 
	 * @param jdfFile
	 *            the JDF instance or JMF message to parse
	 * @param reportFile
	 *            the absolute path to the file to write the validation report
	 *            to
	 * @throws IOException
	 */
	public static void validate(File jdfFile, File reportFile)
			throws IOException {
		validate(jdfFile, new File("../testdata/schema/JDF.xsd"), null,
				reportFile);
	}

	/**
	 * Validates the JDF instance or JMF message read from a file. A validation
	 * report in XML format is written to a file.
	 * 
	 * @param jdf
	 *            the JDF instance or JMF message to parse
	 * @param schemaFile
	 *            The schema file to use for schema validation. If
	 *            <code>null</code> then schema validation is not performed.
	 * @param devcap
	 *            The device capabilities to use for validation. If
	 *            <code>null</code> testing against device capabilities is not
	 *            performed.
	 * @param reportFile
	 *            The file to write the validation XMl report to. If
	 *            <code>null</code> no XML report is written.
	 */
	public static void validate(JDFNode jdf, File schemaFile, JDFJMF devcap,
			File reportFile) throws IOException {
		if (jdf == null) {
			throw new IllegalArgumentException("JDFNode may not be null");
		}
		File jdfFile = createTempFile(".jdf");
		CopyUtils.copy(jdf.toXML(), new FileOutputStream(jdfFile));
		File devcapFile = null;
		if (devcap != null) {
			devcapFile = createTempFile(".xml");
			CopyUtils.copy(devcap.toXML(), new FileOutputStream(devcapFile));
		}
		validate(jdfFile, schemaFile, devcapFile, reportFile);
	}

	/**
	 * Validates the JDF instance or JMF message read from a file. A validation
	 * report in XML format is written to a file.
	 * 
	 * @param jdfFile
	 *            the JDF instance or JMF message to parse
	 * @param schemaFile
	 *            The schema file to use for schema validation. If
	 *            <code>null</code> then schema validation is not performed.
	 * @param devcapFile
	 *            The device capabilities file to use for validation. If
	 *            <code>null</code> testing against device capabilities is not
	 *            performed.
	 * @param reportFile
	 *            The file to write the validation XMl report to. If
	 *            <code>null</code> no XML report is written.
	 * @throws IOException
	 */
	public static void validate(File jdfFile, File schemaFile, File devcapFile,
			File reportFile) throws IOException {
		List args = new ArrayList();

		// JDF
		if (jdfFile == null) {
			throw new IllegalArgumentException("The JDF File may not be null");
		}
		try {
			jdfFile.getCanonicalFile();
		} catch (IOException ioe) {
		}
		args.add(jdfFile.getPath());

		// Dev caps
		if (devcapFile != null) {
			try {
				devcapFile.getCanonicalPath();
			} catch (Exception ioe) {
			}
			args.add("-d " + devcapFile.getPath());
		}

		// XML report
		if (reportFile != null) {
			try {
				reportFile.getCanonicalFile();
			} catch (IOException ioe) {
			}
			args.add("-x " + reportFile.getPath());
		}

		// Schema
		// TODO Maybe we default to the bundled schema? schemaArg = new
		// File("../testdata/schema/JDF.xsd").getAbsolutePath();
		if (schemaFile != null) {
			try {
				schemaFile.getCanonicalFile();
			} catch (IOException ioe) {
			}
			args.add("-L " + schemaFile.getPath());
		}

		// Other options
		args.add("-qcv");

		// Builds command line
		String[] commandLineArgs = (String[]) args.toArray(new String[args
				.size()]);
		validateCommandLine(commandLineArgs);
	}

	/**
	 * Calls CheckJDF using the specified command line.
	 * 
	 * @param commandLineArgs
	 */
	public static void validateCommandLine(String[] commandLineArgs) {
		CheckJDF checker = new CheckJDF();
		checker.setPrint(false);
		checker.validate(commandLineArgs, null);
		checker = null;
	}

	/**
	 * Creates a temp file with a random 16 character name, excluding a suffix.
	 * 
	 * @param suffix
	 *            the file suffix of the temp file
	 * @return the temp file
	 * @throws IOException
	 */
	private static File createTempFile(String suffix) throws IOException {
		// Write input stream to temp file
		String fileName = RandomStringUtils.randomAlphanumeric(16);
		return File.createTempFile(fileName, suffix);
	}

	/**
	 * Looks at a CheckJDF XML report to see if the validated JDF was valid.
	 * 
	 * @param reportFile
	 *            the CheckJDF XML report
	 * @return <code>true</code> if the JDF was valid; <code>false</code>
	 *         otherwise
	 * @throws Exception
	 */
	public static boolean isValid(File reportFile) throws Exception {
		return isValid(new FileReader(reportFile));
	}

	/**
	 * Looks at a CheckJDF XML report to see if the validated JDF was valid.
	 * 
	 * @param reportString
	 *            a string containing the CheckJDF XML report
	 * @return <code>true</code> if the JDF was valid; <code>false</code>
	 *         otherwise
	 * @throws Exception
	 */
	public static boolean isValid(String reportString) throws Exception {
		return isValid(new StringReader(reportString));
	}

	/**
	 * Looks at a CheckJDF XML report to see if the validated JDF was valid.
	 * 
	 * @param reportReader
	 *            a Reader that reads an CheckJDF XML report
	 * @return <code>true</code> if the JDF was valid; <code>false</code>
	 *         otherwise
	 * @throws Exception
	 */
	public static boolean isValid(Reader reportReader) throws Exception {
		// Parse report
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(reportReader);
		// Get report's namespace
		Namespace n = doc.getRootElement().getNamespace();
		// Check for validation failure
		// TODO Verify that this check sufficient
		XPath xp = XPath
				.newInstance("(count(//*[@IsValid='false'])=0) and (count(//jdf:SchemaValidationOutput[not(*)])=1)");
		xp.addNamespace("jdf", n.getURI());
		Boolean passed = (Boolean) xp.selectSingleNode(doc);
		return passed.booleanValue();
	}

}
