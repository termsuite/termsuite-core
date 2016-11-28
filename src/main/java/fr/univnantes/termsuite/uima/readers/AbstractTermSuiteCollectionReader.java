/*******************************************************************************
 * Copyright 2015-2016 - CNRS (Centre National de Recherche Scientifique)
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *******************************************************************************/
package fr.univnantes.termsuite.uima.readers;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.TermSuiteCollection;
import fr.univnantes.termsuite.types.SourceDocumentInformation;

/**
 * An abstract {@link CollectionException} implementation for TermSuite that 
 * recursively load all selected files from an input directory, with customizable file filter 
 * and document text parser.
 * 
 * of an input
 * @author Damien Cram
 *
 */
public abstract class AbstractTermSuiteCollectionReader extends CollectionReader_ImplBase {
	private static final Logger logger = LoggerFactory.getLogger(AbstractTermSuiteCollectionReader.class);
	
	public static final String PARAM_INPUTDIR = "CorpusInputDir";
	@ConfigurationParameter(name = PARAM_INPUTDIR, mandatory=true)
	private String inputDirPath;
	
	public static final String PARAM_ENCODING = "CorpusEncoding";
	@ConfigurationParameter(name = PARAM_ENCODING, mandatory=true)
	private String mEncoding;
	
	public static final String PARAM_LANGUAGE = "CorpusLanguage";
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory=true)
	private Lang mLanguage;

	public static final String PARAM_COLLECTION_TYPE = "CollectionType";
	@ConfigurationParameter(name=PARAM_COLLECTION_TYPE, mandatory=true)
	private String collectionTypeName;
	protected TermSuiteCollection collectionType;


	public static final String PARAM_DROPPED_TAGS = "DroppedTags";
	@ConfigurationParameter(name=PARAM_DROPPED_TAGS, mandatory=false)
	private String droppedTagsStr;
	protected String[] droppedTags;

	public static final String PARAM_TXT_TAGS = "TxtTags";
	@ConfigurationParameter(name=PARAM_TXT_TAGS, mandatory=false)
	private String txtTagsStr;
	protected String[] txtTags;
	
	
	private int mCurrentIndex;
	private List<File> mFiles;
	private long totalFileByteSize = 0;
	private long currentFileByteSize = 0;

	private StringPreparator preparator;

	private Timer progressLoggerTimer;

	@Override
	public void initialize() throws ResourceInitializationException {
		this.mLanguage = Lang.forName((String) getConfigParameterValue(PARAM_LANGUAGE));
		this.mEncoding = (String) getConfigParameterValue(PARAM_ENCODING);
		this.inputDirPath = (String) getConfigParameterValue(PARAM_INPUTDIR);
		this.collectionTypeName = (String) getConfigParameterValue(PARAM_COLLECTION_TYPE);
		this.txtTagsStr = (String) getConfigParameterValue(PARAM_TXT_TAGS);
		this.droppedTagsStr = (String) getConfigParameterValue(PARAM_DROPPED_TAGS);

		
		
		this.collectionType = TermSuiteCollection.valueOf(collectionTypeName);
		if(this.droppedTagsStr != null)
			this.droppedTags = Splitter.on(',').splitToList(droppedTagsStr).toArray(new String[]{});
		if(this.txtTagsStr != null)
			this.txtTags = Splitter.on(',').splitToList(txtTagsStr).toArray(new String[]{});

		this.preparator = new StringPreparator();
		File directory = new File(inputDirPath);
		this.mCurrentIndex = 0;

		// get list of files (not subdirectories) in the specified directory
		this.mFiles = new LinkedList<File>();
		FilenameFilter fileFilter = getFileFilter();
		File[] files = directory.listFiles(fileFilter);
		for (int i = 0; i < files.length; i++) {
			if (!files[i].isDirectory()) {
				this.mFiles.add(files[i]);
				this.totalFileByteSize += files[i].length();
			}
		}
		Collections.shuffle(this.mFiles);
//		Collections.sort(this.mFiles, SizeFileComparator.SIZE_REVERSE);

		logger.info("Initializing collection reader on input dir {} (enc: {}, lang: {}, type: {})", 
				inputDirPath, 
				this.mEncoding,
				this.mLanguage,
				this.collectionType.name().toLowerCase()
				);
		
		
		// initialize the periodic logger progress
		progressLoggerTimer = new Timer("Collection Progress Timer");
	}
	
//	protected abstract TermSuiteCollection getCollectionType();

	@Override
	public void getNext(CAS cas) throws IOException, CollectionException {
		if(mCurrentIndex == 0) {
			logger.info("Reading collection {} ({} documents)", inputDirPath, mFiles.size());
			progressLoggerTimer.schedule(new TimerTask() {
				private long lastSize = 0;
				private long lastTop = System.currentTimeMillis();
				
				@Override
				public void run() {
					long top = System.currentTimeMillis();
					long size = currentFileByteSize;
					AbstractTermSuiteCollectionReader.logger.info("{}% - {} processed out of {} ({} Mb/s) - Processing file {} (doc. {} out of {})", 
							String.format("%.2f", ((float)currentFileByteSize*100)/totalFileByteSize),
							fr.univnantes.termsuite.utils.FileUtils.humanReadableByteCount(currentFileByteSize, true),
							fr.univnantes.termsuite.utils.FileUtils.humanReadableByteCount(totalFileByteSize, true),
							String.format("%.2f", 0.001*((float)size-lastSize)/((float)Math.max(1, top-lastTop))),
							mFiles.get(mCurrentIndex).getName(),
							mCurrentIndex,
							mFiles.size()
							);
					lastSize = size;
					lastTop = top;
				}
			}, 5000l, 5000l);
		}
		File file = mFiles.get(mCurrentIndex++);
		this.currentFileByteSize += file.length();
		
		logger.debug("Reading file ("+this.mCurrentIndex+"/"+this.mFiles.size()+") " + file.getAbsolutePath());
		fillCas(cas, file);
		if(!hasNext())
			this.lastFileRead();
	}

	protected void fillCas(CAS cas, File file) throws IOException, CollectionException {
		String uri = file.toURI().toString();
		SourceDocumentInformation sdi;
		try {
			sdi = new SourceDocumentInformation(cas.getJCas());
			sdi.setUri(uri);
			String text = getDocumentText(file.getAbsolutePath(), this.mEncoding);
			cas.setDocumentLanguage(mLanguage.getCode());
			cas.setDocumentText(preparator.prepare(text));
			sdi.setDocumentSize((int)file.length());
			sdi.setCumulatedDocumentSize(this.currentFileByteSize);
			sdi.setCorpusSize(this.totalFileByteSize);
			sdi.setBegin(0);
			sdi.setEnd(text.length());
			sdi.setOffsetInSource(0);
			sdi.setDocumentIndex(mCurrentIndex);
			sdi.setNbDocuments(this.mFiles.size());
			
			sdi.setLastSegment(mCurrentIndex == mFiles.size() - 1);
			sdi.addToIndexes();
		} catch (CASException e) {
			throw new CollectionException(e);
		}
	}

	@Override
	public boolean hasNext() throws IOException, CollectionException {
		return mCurrentIndex < mFiles.size();
	}

	@Override
	public Progress[] getProgress() {
		return new Progress[]{
			     new ProgressImpl(mCurrentIndex,mFiles.size(),Progress.ENTITIES)};
	}

	@Override
	public void close() throws IOException {
	}
	
	public List<File> getFiles() {
		return ImmutableList.copyOf(mFiles);
	}

	/**
	 * The {@link FilenameFilter} for selecting input files to read.
	 * @return
	 */
	protected FilenameFilter getFileFilter() {
		// accepts all files in default implementation
		return new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return true;
			}
		};

	}
	
	/**
	 * Gives the document text to set from the input file URI.
	 * 
	 * @param uri
	 * @return
	 * @throws IOException
	 */
	protected abstract String getDocumentText(String uri, String encoding) throws IOException;
	
	/**
	 * A hook that is executed after the last input files has been read. (Before the last CAS pipeline runs)
	 */
	protected void lastFileRead() {
		logger.info("Finished reading collection {}", inputDirPath);
		progressLoggerTimer.cancel();
	}
}
