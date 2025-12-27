import { FormatMarkdownPipe } from './format-markdown.pipe';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { TestBed } from '@angular/core/testing';

describe('FormatMarkdownPipe', () => {
  let pipe: FormatMarkdownPipe;
  let sanitizer: DomSanitizer;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    sanitizer = TestBed.inject(DomSanitizer);
    pipe = new FormatMarkdownPipe(sanitizer);
  });

  it('should create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  it('should return empty string for empty input', () => {
    expect(pipe.transform('')).toBe('');
  });

  it('should return empty string for null/undefined input', () => {
    expect(pipe.transform(null as any)).toBe('');
    expect(pipe.transform(undefined as any)).toBe('');
  });

  it('should convert **bold** to strong tags', () => {
    const result = pipe.transform('This is **bold** text') as any;
    expect(result.changingThisBreaksApplicationSecurity || result).toContain('<strong>bold</strong>');
  });

  it('should convert __bold__ to strong tags', () => {
    const result = pipe.transform('This is __bold__ text') as any;
    expect(result.changingThisBreaksApplicationSecurity || result).toContain('<strong>bold</strong>');
  });

  it('should convert *italic* to em tags', () => {
    const result = pipe.transform('This is *italic* text') as any;
    expect(result.changingThisBreaksApplicationSecurity || result).toContain('<em>italic</em>');
  });

  it('should convert _italic_ to em tags', () => {
    const result = pipe.transform('This is _italic_ text') as any;
    expect(result.changingThisBreaksApplicationSecurity || result).toContain('<em>italic</em>');
  });

  it('should convert `code` to code tags', () => {
    const result = pipe.transform('Use `const` keyword') as any;
    expect(result.changingThisBreaksApplicationSecurity || result).toContain('<code>const</code>');
  });

  it('should convert newlines to br tags', () => {
    const result = pipe.transform('Line 1\nLine 2') as any;
    expect(result.changingThisBreaksApplicationSecurity || result).toContain('<br>');
  });

  it('should escape HTML special characters', () => {
    const result = pipe.transform('<script>alert("xss")</script>') as any;
    const html = result.changingThisBreaksApplicationSecurity || result;
    expect(html).toContain('&lt;script&gt;');
    expect(html).not.toContain('<script>');
  });

  it('should handle combined formatting', () => {
    const result = pipe.transform('**bold** and *italic* and `code`') as any;
    const html = result.changingThisBreaksApplicationSecurity || result;
    expect(html).toContain('<strong>bold</strong>');
    expect(html).toContain('<em>italic</em>');
    expect(html).toContain('<code>code</code>');
  });
});

