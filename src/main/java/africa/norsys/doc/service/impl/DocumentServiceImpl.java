package africa.norsys.doc.service.impl;

import africa.norsys.doc.repository.DocumentRepository;
import africa.norsys.doc.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;

}
