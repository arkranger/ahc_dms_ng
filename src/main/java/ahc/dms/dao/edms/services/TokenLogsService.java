package ahc.dms.dao.edms.services;

import ahc.dms.dao.edms.entities.TokenLogs;
import ahc.dms.dao.edms.repositories.TokenLogsRepository;
import ahc.dms.exceptions.ResourceNotFoundException;
import ahc.dms.payload.dto.TokenLogDto;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenLogsService {

    @Autowired
    private TokenLogsRepository tokenLogsRepository;
    @Autowired
    private ModelMapper modelMapper;
    private final Logger logger = LoggerFactory.getLogger(TokenLogsService.class);

    @Transactional (transactionManager = "msDmsTransactionManager")
    public TokenLogDto saveToken(TokenLogDto tokenLogDto) {
        logger.info("saving token!!!!");
        TokenLogs newToken = tokenLogsRepository.save(modelMapper.map(tokenLogDto, TokenLogs.class));
        return modelMapper.map(newToken, TokenLogDto.class);
    }

    @Transactional (transactionManager = "msDmsTransactionManager")
    public TokenLogDto revokeToken(Long tokenId) {
        TokenLogs token = tokenLogsRepository.findById(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Token", "Token Id", tokenId));
        token.setTokenStatus(false);
        TokenLogs deletedToken = tokenLogsRepository.save(token);
        return modelMapper.map(deletedToken, TokenLogDto.class);

    }

    public TokenLogDto findTokenByUsername(String username) {
        TokenLogs token = tokenLogsRepository.findByUsername(username);
        if (token != null)
            return modelMapper.map(token, TokenLogDto.class);
        return null;
    }

    public TokenLogDto findToken(String token, String username) {
        TokenLogs existingToken = tokenLogsRepository.findByUsernameAndJwtToken(username, token);
        if (existingToken != null)
            return modelMapper.map(existingToken, TokenLogDto.class);
        return null;
    }
}
