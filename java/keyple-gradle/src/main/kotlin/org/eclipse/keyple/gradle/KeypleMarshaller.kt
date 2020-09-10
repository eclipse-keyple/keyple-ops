package org.eclipse.keyple.gradle;

import org.xml.sax.InputSource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.xml.bind.JAXB
import javax.xml.bind.JAXBContext
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.stream.StreamResult

class KeypleMarshaller {

    fun marshal(o: Any): InputStream {
        val outputStream = ByteArrayOutputStream()
        val streamResult = StreamResult()
        streamResult.outputStream = outputStream
        val marshaller = JAXBContext.newInstance(o.javaClass).createMarshaller()
        marshaller.marshal(o, streamResult)
        println(outputStream.toString())
        return ByteArrayInputStream(outputStream.toByteArray())
    }

    fun <T> unmarshal(response: InputStream, clazz: Class<T>?): T {
        val sax = SAXParserFactory.newInstance()
        sax.isNamespaceAware = false
        try {
            val reader = sax.newSAXParser().xmlReader
            val inputSource = InputSource(response)
            val saxSource = SAXSource(reader, inputSource)
            return JAXB.unmarshal(saxSource, clazz)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}
